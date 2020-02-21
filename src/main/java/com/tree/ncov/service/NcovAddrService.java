package com.tree.ncov.service;

import cn.hutool.core.date.TimeInterval;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tree.ncov.addrdata.entity.NcovAddrDetail;
import com.tree.ncov.addrdata.entity.NcovResult;
import com.tree.ncov.addrdata.repository.AddrRepository;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.tree.ncov.constant.Constants.INSERT_NCOV_SQL_ADDR_PREFIX;
import static com.tree.ncov.constant.Constants.TRUNCATE_ADDR_TABLE;

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

    @Value("${ncov.githubdata.truncateable:false}")
    boolean truncateable;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddrRepository addrRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void executeCompareAndUpdate(List list, Object obj) throws IOException {
        List<NcovAddrDetail> remoteAddrDetails = list;
        List<NcovAddrDetail> dbAddrDetailList = (List<NcovAddrDetail> )obj;

        List<NcovAddrDetail> updateList = new ArrayList<>();
        remoteAddrDetails.forEach(remoteAddrDetail -> {
            String remoteAddr = remoteAddrDetail.getAddress();
            if(!dbAddrDetailList.contains(remoteAddrDetail)){
                updateList.add(remoteAddrDetail);
                log.info("==> 执行[executeCompareAndUpdate] 该对象不存在与DB， 需要更新,{}",JSON.toJSONString(remoteAddrDetail));
            }
        });
        initBatchUpdate(updateList);

    }

    @Override
    protected Object loadTodayData() throws IOException {
        List<NcovAddrDetail> addrDetailList =  addrRepository.findAll();

        return addrDetailList;
    }

    /**
     * 下载远程文件到本地
     * @throws IOException
     */
    public void downloadFile2Local() throws IOException {
        TimeInterval timeInterval = new TimeInterval();
        List<NcovAddrDetail> ncovAddrDetails = readFileFromRemote();
        log.info("==>[downloadFile2Local], 下载远程文件到本地:{}",localJsonUrl);
        FileUtils.writeStringToFile(new File(localJsonUrl),
                Json2Csv(JSON.toJSONString(ncovAddrDetails)));
        log.info("==>执行[downloadFile2Local] , 总花费时间：{}",timeInterval.interval());
    }

    @Override
    public List<NcovAddrDetail> readFileFromRemote() throws IOException {
        TimeInterval timeInterval = new TimeInterval();
        log.info("==>[readFileFromRemote],读取并解析远程: {}",remoteJsonUrl);
        NcovResult o = restTemplate.getForObject(remoteJsonUrl, NcovResult.class);

        List<NcovAddrDetail> ncovAddrDetails = o.getData();
        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
        int invalidCount = 0;
        int allCount = ncovAddrDetails.size();
        while (it.hasNext()) {
            NcovAddrDetail addrDetail = it.next();
            //去除远程无效数据
            if (StringUtils.isEmpty(addrDetail.getAddress())
                    || StringUtils.isEmpty(addrDetail.getLongitude())
                    | StringUtils.isEmpty(addrDetail.getLatitude())) {
                invalidCount++;
                log.debug("无效数据， 忽略：{}", JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
                it.remove();
                continue;
            }
            addrDetail.setLongitudeLatitude(addrDetail.getLatitude()+","+addrDetail.getLatitude());
        }
        log.info("==>[readFileFromRemote],读取远程文件并做简单数据处理 , 总条数【{}】，无效条数【{}】," +
                        " 去除无效数据后条数【{}】，重复数据11条，故实际为【{}】, 总花费时间【{}】毫秒",
                allCount,invalidCount,ncovAddrDetails.size(),(ncovAddrDetails.size()-11),
                timeInterval.interval());

        return ncovAddrDetails;
    }

    /**
     * 读取本地CSV
     *
     * @throws IOException
     */
    @Override
    public List readFileFromLocal() throws IOException {
        TimeInterval timeInterval = new TimeInterval();
        log.info("==>[readFileFromLocal], 读取本地文件:{}",localJsonUrl);
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
            addrDetail.setLongitudeLatitude(longtitude+","+latitude);

            if (StringUtils.isEmpty(addrDetail.getAddress())
                    || StringUtils.isEmpty(addrDetail.getLongitude())
                    | StringUtils.isEmpty(addrDetail.getLatitude())) {
                log.info("无效数据， 忽略：{}" ,JSON.toJSONString(addrDetail));
                continue;
            }
            ncovAddrDetails.add(addrDetail);

            i++;
        }
        log.info("==>[readFileFromLocal], 读取本地文件并做简单数据处理，总花费时间【{}】毫秒",
                timeInterval.interval());

        return ncovAddrDetails;
    }


    @Override
    public void initTable() {
        if(truncateable) {
            jdbcTemplate.execute(TRUNCATE_ADDR_TABLE);
        }
    }


//    @Override
//    public void putDataInRedis(List ncovAddrDetails) {
//        long start = System.currentTimeMillis();
//        Map<String/*address*/, NcovAddrDetail> addrDetailMap = new HashMap<>();
//        int allCount = ncovAddrDetails.size();
//        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
//        int duplicateCount = 0;
//        while (it.hasNext()) {
//            NcovAddrDetail addrDetail = it.next();
//            if (!addrDetailMap.containsKey(addrDetail.getAddress())) {
//                addrDetailMap.put(addrDetail.getAddress(), addrDetail);
//            } else {
//                duplicateCount++;
//                it.remove();
//                log.debug("重复地址， 忽略：{}", JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
//            }
//        }
//        redisService.put(CBN_DATA_REDIS_KEY, addrDetailMap);
//        log.info("==>执行[putDataInRedis], 总条数【{}】, 重复条数【{}】，去除重复数据之后实际条数【{}】, 共花费【{}】毫秒",
//                allCount, duplicateCount, ncovAddrDetails.size(), (System.currentTimeMillis() - start));
//
//    }


    @Override
    public void initBatchUpdate(List ncovAddrDetails) {
        TimeInterval timeInterval = new TimeInterval();
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
                    .append("'").append(ncovAddrDetail.getLongitude()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getLongitudeLatitude()).append("'")
                    .append(")");

            //每100条执行一次批量操作
            if (insertCount == 99) {
                valueSql.append(";");
                jdbcTemplate.execute(sql.append(valueSql).toString());

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

        //最后不满100次， 还执行一次
        if (valueSql.length() != 0) {
            String s = valueSql.substring(0, valueSql.length() - 1);
            jdbcTemplate.execute(sql.append(s).toString());
            executeSqlNum++;

        }
        log.info("==>[initBatchUpdate],执行数据库【{}】次，数据共【{}】条, 共花费【{}】毫秒",
                executeSqlNum ,addrDetails.size(),timeInterval.interval());
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
