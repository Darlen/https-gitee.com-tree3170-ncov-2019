package com.tree.ncov.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.cbndata.entity.NcovResult;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.tree.ncov.constant.Constants.*;
import static com.tree.ncov.constant.Constants.CBN_DATA_CSV_FILE_PATH;

/**
 * @ClassName com.tree.ncov
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-12 21:23
 * @Version 1.0
 */
@Slf4j
@Service
public class NcovAddrService extends AbstractService {

    @Value("${ncov.ds.name:mysql}")
    private String dsName;

    @Value("${ncov.cbndata.truncate:false}")
    boolean truncate;

    @Value("${ncov.githubdata.from}")
    private String from;

    @Value("${ncov.cbndata.remote.url}")
    private String remoteJsonUrl;

    @Value("${ncov.cbndata.remote.filename}")
    private String remoteJsonFilename;

    /**
     * 由于网络不通， 只能使用本地json文件
     */
    @Value("${ncov.cbndata.local.url}")
    private String localJsonUrl;

    @Value("${ncov.cbndata.local.filename}")
    private String localJsonFilename;

    @Autowired
    private RedisService redisService;

    @Override
    public void compareAndUpdate() throws Exception {
        List<NcovAddrDetail> remoteAddrDetails = readFileFromRemote();
        Map<String, JSONObject> addrDetailMap = (Map<String, JSONObject>) redisService.get(CBN_DATA_REDIS_KEY);

        //如果redis不存在， 则重新初始化数据
        if (addrDetailMap.size() == 0) {
            initDataFromLocal();
            addrDetailMap = (Map<String, JSONObject>) redisService.get(CBN_DATA_REDIS_KEY);
        }

        List<NcovAddrDetail> addAddrDetails = new ArrayList<>();
        for (NcovAddrDetail remoteAddrDetail : addAddrDetails) {
            String address = remoteAddrDetail.getAddress();
            if (addrDetailMap.get(address) == null) {
                addAddrDetails.add(remoteAddrDetail);
                addrDetailMap.put(address, JSON.parseObject(JSON.toJSONString(remoteAddrDetail)));
            }
            NcovAddrDetail redisAddrDetail = JSON.toJavaObject(addrDetailMap.get(address), NcovAddrDetail.class);
        }
        log.info("[NcovAddrService] compareAndUpdate===》增加对象：{}", JSON.toJSONString(addAddrDetails));
        redisService.put(CBN_DATA_REDIS_KEY, addrDetailMap);


        batchUpdate(addAddrDetails);

    }

    @Override
    public void downloadFile2Local() throws IOException {
        long start = System.currentTimeMillis();
        List<NcovAddrDetail> ncovAddrDetails = readFileFromRemote();
        log.info("==>[downloadFile2Local], 下载远程文件到本地:{}",localJsonUrl);
        FileUtils.writeStringToFile(new File(localJsonUrl),
                Json2Csv(JSON.toJSONString(ncovAddrDetails)));
        log.info("==>执行[downloadFile2Local] , 总花费时间：{}",(System.currentTimeMillis() - start));
    }

    @Override
    public List<NcovAddrDetail> readFileFromRemote() throws IOException {
        long start = System.currentTimeMillis();
        RestTemplate restTemplate = new RestTemplate();
        log.info("==>[readFileFromRemote], 读取并解析远程url:{}",remoteJsonUrl);
        NcovResult o = restTemplate.getForObject(remoteJsonUrl, NcovResult.class);

        List<NcovAddrDetail> ncovAddrDetails = o.getData();
        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
        int invalidCount = 0;
        int allCount = ncovAddrDetails.size();
        while (it.hasNext()) {
            NcovAddrDetail addrDetail = it.next();
            if (StringUtils.isEmpty(addrDetail.getAddress())
                    || StringUtils.isEmpty(addrDetail.getLongitude())
                    | StringUtils.isEmpty(addrDetail.getLatitude())) {
                invalidCount++;
                log.info("无效数据， 忽略：{}", JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
                it.remove();
                continue;
            }
        }
        log.info("==>执行[readFileFromRemote] , 总条数【{}】，无效条数【{}】， 去除无效数据后条数【{}】, 总花费时间【{}】毫秒",
                allCount,invalidCount,ncovAddrDetails.size(),(System.currentTimeMillis() - start));

        return ncovAddrDetails;
    }

    /**
     * 读取本地CSV
     *
     * @throws IOException
     */
    @Override
    public List readFileFromLocal() throws IOException {
        long start = System.currentTimeMillis();
        log.info("==>执行[readFileFromLocal], 读取本地文件:{}",localJsonUrl);
        FileReader fileReader = new FileReader(new File(localJsonUrl));
        BufferedReader br = new BufferedReader(fileReader);

        String line = null;
        int i = 0;
        NcovAddrDetail addrDetail = null;
        String addr = null;
        String latitude = null;
        String longtitude = null;
        List<NcovAddrDetail> ncovAddrDetails = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            //第一行为表头， 忽略
            if (i == 0) {
                i++;
                continue;
            }
            String[] data = line.split(",");
            addrDetail = new NcovAddrDetail();
            //TODO 改为读JSON文件
            addr = data[0];
            latitude = data[4];
            longtitude = data[6];
            addrDetail.setAddress(addr);
            addrDetail.setProvince(data[1]);
            addrDetail.setCity(data[2]);
            addrDetail.setDistrict(data[3]);
            addrDetail.setLatitude(latitude);
            int count = StringUtils.isEmpty(data[5]) ? 0 : Integer.valueOf(data[5]);
            addrDetail.setCount(count);
            addrDetail.setLongitude(longtitude);

            if (StringUtils.isEmpty(addrDetail.getAddress())
                    || StringUtils.isEmpty(addrDetail.getLongitude())
                    | StringUtils.isEmpty(addrDetail.getLatitude())) {
                log.info("无效数据， 忽略：{}" ,JSON.toJSONString(addrDetail));
                continue;
            }
            ncovAddrDetails.add(addrDetail);

            i++;
        }
        log.info("==>执行[readFileFromLocal] 总花费时间【{}】毫秒", (System.currentTimeMillis() - start));

        return ncovAddrDetails;
    }


    @Override
    public void initTable() {
        if(truncate) {
            DsUtil.execute(TRUNCATE_ADDR_TABLE, null);
        }
    }


    @Override
    public void putDataInRedis(List ncovAddrDetails) {
        long start = System.currentTimeMillis();
        Map<String/*address*/, NcovAddrDetail> addrDetailMap = new HashMap<>();
        int allCount = ncovAddrDetails.size();
        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
        int duplicateCount = 0;
        while (it.hasNext()) {
            NcovAddrDetail addrDetail = it.next();
            if (!addrDetailMap.containsKey(addrDetail.getAddress())) {
                addrDetailMap.put(addrDetail.getAddress(), addrDetail);
            } else {
                duplicateCount++;
                it.remove();
                log.info("重复地址， 忽略：{}", JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
            }
        }
//        redisService.hashSet(CBN_DATA_REDIS_KEY+"HASH", );
        redisService.put(CBN_DATA_REDIS_KEY, addrDetailMap);
        log.info("==>执行[putDataInRedis], 总条数【{}】, 重复条数【{}】，去除重复数据之后实际条数【{}】, 共花费【{}】毫秒",
                allCount, duplicateCount, ncovAddrDetails.size(), (System.currentTimeMillis() - start));

    }


    @Override
    public void batchUpdate(List ncovAddrDetails) {
        int insertCount = 0;
        int executeSqlNum = 0;
        StringBuilder sql = new StringBuilder(1024 * 50);
        StringBuilder valueSql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
        List<NcovAddrDetail> addrDetails = (List<NcovAddrDetail>) ncovAddrDetails;
        for(NcovAddrDetail ncovAddrDetail : addrDetails){
            valueSql.append("(")
                    .append("'").append(ncovAddrDetail.getAddress()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getProvince()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getCity()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getDistrict()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getLatitude()).append("'").append(",")
                    .append(ncovAddrDetail.getCount()).append(",")
                    .append("'").append(ncovAddrDetail.getLongitude()).append("'")
                    .append(")");

            if (insertCount == 99) {
                valueSql.append(";");
                DsUtil.execute(sql.append(valueSql).toString(),null);

                insertCount = 0;
                executeSqlNum++;

                valueSql = new StringBuilder(1024 * 50);
                sql = new StringBuilder(1024 * 500);
                sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
            } else {
                valueSql.append(",");
                insertCount++;
            }
        }

        if (valueSql.length() != 0) {
            String s = valueSql.substring(0, valueSql.length() - 1);
            DsUtil.execute(sql.append(s).toString(),null);
            executeSqlNum++;

        }
        log.info("执行数据库【{}】次，数据共【{}】条",executeSqlNum ,addrDetails.size());
    }

    public void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
        valueSql = new StringBuilder(1024 * 50);
        sql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
    }


    public static String Json2Csv(String jsonstr) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonstr);
        //在内容开头加入UTF-8的BOM标识，如果用Excel打开没有这个会乱码的
        String UTF_BOM_INFO = new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        String csv = UTF_BOM_INFO + CDL.toString(jsonArray);
        return csv;
    }


}
