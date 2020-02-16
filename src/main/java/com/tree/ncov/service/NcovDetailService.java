package com.tree.ncov.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.github.entity.NcovCityDetail;
import com.tree.ncov.github.entity.NcovCountryResult;
import com.tree.ncov.github.entity.NcovProvDetail;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tree.ncov.constant.Constants.*;
import static com.tree.ncov.constant.Constants.INSERT_NCOV_SQL_PREFIX;

/**
 * @ClassName com.tree.ncov.service
 * Description:
 * <p>
 *
 * </p>
 * @Author tree
 * @Date 2020-02-15 11:50
 * @Version 1.0
 */
@Slf4j
@Service
public class NcovDetailService extends AbstractService {

    @Value("${ncov.ds.name:mysql}")
    private String dsName;

    @Value("${ncov.githubdata.truncate:false}")
    boolean truncate;

    @Value("${ncov.githubdata.from}")
    private String from;

    @Value("${ncov.githubdata.remote.json.url}")
    private String remoteJsonUrl;

    @Value("${ncov.githubdata.remote.json.filename}")
    private String remoteJsonFilename;

    @Value("${ncov.githubdata.remote.zip.url}")
    private String remoteZipUrl;

    @Value("${ncov.githubdata.remote.zip.filename}")
    private String remoteZipFilename;

    /**
     * 由于网络不通， 只能使用本地json文件
     */
    @Value("${ncov.githubdata.local.json.url}")
    private String localJsonUrl;

    @Value("${ncov.githubdata.local.json.filename}")
    private String localJsonFilename;

    /**
     * 本地CSV文件
     */
    @Value("${ncov.githubdata.local.csv.url}")
    private String localCsvUrl;

    @Value("${ncov.githubdata.local.csv.filename}")
    private String localCsvFilename;

    @Autowired
    private RedisService redisService;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
     */
    static Map<String, NcovCityDetail> addrDetailMap = new HashMap<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");


    @Override
    public  void initDataFromRemote() throws Exception {
        log.warn("*****************************");
        log.warn("#由于远程只有当天的数据，该方法[initDataFromRemote]不支持从远程初始化数据#");
        log.warn("*****************************");
    }

    /**
     * 如果不是当天数据， 忽略
     * 拿当天数据做对比， 如果存在， 则比较更新时间，如果不存在， 则添加
     *
     * @throws Exception
     */
    @Override
    public void compareAndUpdate() throws Exception {
        //1. 读取远程文件
        List<NcovProvDetail> remoteChinaProvDetails = readFileFromRemote();

        handleRemoteData(remoteChinaProvDetails);

        //2. 从redis获取当天对象
        Map<String/*省*/, JSONObject/*NcovProvDetail*/> curDayProvDetailMap =
                (Map<String, JSONObject> )redisService.get(GITHUBU_DATA_CITY_BY_DAY_REDIS_KEY
                        +sdf3.format(remoteChinaProvDetails.get(0).getUpdateTime()));
        /*
         * 按年月日存储所有省集合对象， 取每天省的最新数据
         *
         * 算法：
         *     如果redis中没有包含年月日，则增加省，增加城市， 并更新redis
         *     否则远程的 省份更新时间 > redis中省份的更新时间， 则更新
         */
        remoteChinaProvDetails.forEach(remoteProvDetail -> {
            //3. 处理
            handleCompareResult(remoteProvDetail,curDayProvDetailMap);

        });

    }

    /**
     * 处理比较后的结果
     * 算法：按年月日存储所有省集合对象， 取每天省的最新数据
     *  如果redis中没有包含年月日，则增加省，增加城市， 并更新redis
     *  否则远程的 省份更新时间 > redis中省份的更新时间， 则更新
     *
     * @param remoteProvDetail
     * @param curDayProvDetailMap
     */
    private void handleCompareResult(NcovProvDetail remoteProvDetail,
                Map<String/*省*/, JSONObject/*NcovProvDetail*/> curDayProvDetailMap ) {
        Date updateTime = remoteProvDetail.getUpdateTime();
        String yearMonthDay = sdf3.format(updateTime);
        String province = remoteProvDetail.getProvinceName();
        //如果当天数据为空，则新增
        if(curDayProvDetailMap == null || curDayProvDetailMap.size() == 0){
            //新增
            batchUpdate(remoteProvDetail.getCities());
            //TODO 增加省
            curDayProvDetailMap.put(province,JSON.parseObject(JSON.toJSONString(remoteProvDetail)));
            redisService.put(GITHUBU_DATA_CITY_BY_DAY_REDIS_KEY+yearMonthDay,curDayProvDetailMap);
        }

        //如果当前数据不为空， 且update时间 > redis中时间，则更新
        JSONObject jsonObject = curDayProvDetailMap.get(province);
        NcovProvDetail redisProvDetail = JSON.toJavaObject(jsonObject, NcovProvDetail.class);
        if(updateTime.getTime() > redisProvDetail.getUpdateTime().getTime()){
            //更新
            List<NcovCityDetail> remoteCities = remoteProvDetail.getCities();
            //TODO 更新省

            //删除当天的这个省份的所有数据，
            String mysqlDeleteSql = "delete from ncov_detail where provinceName='"+province+"', TO_DAYS(updateTime) = TO_DAYS('"+sdf2.format(updateTime)+"')";
            String pgDeleteSql = "delete FROM ncov_detail WHERE finish_time BETWEEN TIMESTAMP'"+yearMonthDay+"' AND TIMESTAMP'"+yearMonthDay+" 23:59:59'";
//                DsUtil.execute("mysql".equals(dsName)? mysqlDeleteSql: pgDeleteSql, null);
            jdbcTemplate.execute("mysql".equals(dsName)? mysqlDeleteSql: pgDeleteSql);
            //插入当前的这个省份的所有数据
            batchUpdate(remoteCities);
        }
    }


    /**
     * 把省份相关的字段塞进城市中
     * @param remoteChinaProvDetails
     */
    private void handleRemoteData(List<NcovProvDetail> remoteChinaProvDetails) {
        remoteChinaProvDetails.forEach(provDetail -> {
            Date updateTime = provDetail.getUpdateTime();
            List<NcovCityDetail> cityDetails = provDetail.getCities();
            cityDetails.forEach(cityDetail -> {
                cityDetail.setProvCurConfirmCount(provDetail.getCurConfirmCount());
                cityDetail.setProvinceConfirmedCount(provDetail.getConfirmedCount());
                cityDetail.setProvinceSuspectedCount(provDetail.getSuspectedCount());
                cityDetail.setProvinceDeadCount(provDetail.getDeadCount());
                cityDetail.setProvinceCuredCount(provDetail.getCuredCount());
                cityDetail.setUpdateTime(updateTime);
                cityDetail.setProvinceName(provDetail.getProvinceName());
            });
        });
    }

    @Override
    public List readFileFromLocal() throws IOException {
        long start = System.currentTimeMillis();
        String line = null;
        int i = 0;
        NcovCityDetail detail = null;
        String province = null;
        String city = null;
        List<NcovCityDetail> ncovCityDetails = new ArrayList<>();
        log.info("==>[readFileFromLocal], 读取本地CSV文件:{}",localCsvUrl);
        //issue : 由于csv有的字段中包含逗号，正常读取每行已经无法用逗号分隔来计算 故引入opencsv
        try (CSVReader csvReader = new CSVReaderBuilder(new BufferedReader
                //GITHUB_DATA_CSV_FILE_PATH
                (new InputStreamReader(new FileInputStream(new File(localCsvUrl)), "UTF-8")))
                .build()) {
            Iterator<String[]> iterator = csvReader.iterator();
            while (iterator.hasNext()) {
                //第一行为表头， 忽略
            /*
            provinceName	provinceEnglishName	cityName	cityEnglishName
            province_confirmedCount	province_suspectedCount	province_curedCount	province_deadCount
            city_confirmedCount	city_suspectedCount	city_curedCount	city_deadCount	updateTime

            provinceName	provinceEnglishName	cityName	cityEnglishName
            province_confirmedCount	province_suspectedCount	province_curedCount	province_deadCount	city_confirmedCount	city_suspectedCount	city_curedCount	city_deadCount	updateTime
             */
                String[] data = iterator.next();
                if (i == 0) {
                    i++;
                    continue;
                }
                detail = new NcovCityDetail();
                province = data[0];
                city = data[2];
                detail.setProvinceName(province);
                detail.setCityName(city);
                try {
                    detail.setProvinceConfirmedCount(Long.valueOf(data[4]));
                    detail.setProvinceSuspectedCount(Long.valueOf(data[5]));
                    detail.setProvinceCuredCount(Long.valueOf(data[6]));
                    detail.setProvinceDeadCount(Long.valueOf(data[7]));

                    detail.setCityConfirmedCount(Long.valueOf(data[8]));
                    detail.setCitySuspectedCount(Long.valueOf(data[9]));
                    detail.setCityCuredCount(Long.valueOf(data[10]));
                    detail.setCityDeadCount(Long.valueOf(data[11]));
                }catch (NumberFormatException e){
                    System.err.println("===line" + line);
                    throw e;
                }

                Date updateTime = null;
                try {
                    updateTime = sdf.parse(data[12]);
                } catch (Exception e) {
                    try {
                        updateTime = sdf2.parse(data[12]);
                    } catch (Exception e1) {
                        System.err.println(data[12] + "===" + JSON.toJSONString(detail));
                    }
                }
                detail.setUpdateTime(updateTime);
                ncovCityDetails.add(detail);

                String yearMonthDay = sdf3.format(updateTime);
                String provCity = province + city;

                i++;
            }
        }
//        putDataInRedis(ncovCityDetails);
        log.info("==>执行[readFileFromLocal] 总花费时间【{}】毫秒", (System.currentTimeMillis() - start));

        return ncovCityDetails;
    }

    /**
     * 远程json是一天的数据， 故该方法仅用于做更新的时候使用(compareAndUpdate)
     * @return
     * @throws IOException
     */
    @Override
    public List readFileFromRemote() throws IOException {
        long start = System.currentTimeMillis();
        log.info("==>[readFileFromRemote], 读取远程JSON文件， 注意由于github网络不通，从本地文件读取:{}",localJsonUrl);
        //注意：由于github不通， 只能到本地读取json文件
        FileReader fileReader = new FileReader(new File(localJsonUrl));
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        StringBuilder sb = new StringBuilder(1024 * 50);
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        NcovCountryResult result = JSON.parseObject(sb.toString(), NcovCountryResult.class);
        //处理数据, 获取有效的中国省市数据
        List<NcovProvDetail> provDetails = result.getResults();
        List<NcovProvDetail> chinaProvDetails = new ArrayList<>();
        Map<String/*省*/, List<NcovCityDetail>> chinaProvCityMap = new HashMap<>();

        provDetails.forEach(ncovProvDetail -> {
            //只获取国家为中国的数据
            if (ncovProvDetail.getCountry().indexOf("中国") != -1) {
                chinaProvDetails.add(ncovProvDetail);
                chinaProvCityMap.put(ncovProvDetail.getProvinceName(), ncovProvDetail.getCities());
            }
        });
        log.info("==>执行[readFileFromRemote] , 总条数【{}】，总花费时间：{}", (System.currentTimeMillis() - start));
        return chinaProvDetails;
    }


    @Override
    public void initTable() {
        if(truncate) {
            DsUtil.execute(TRUNCATE_DETAIL_TABLE, null);
        }
    }


    @Override
    public void putDataInRedis(List ncovCityDetails) {
        long start = System.currentTimeMillis();
        /**
         * 按年月日存储所有省集合对象， 取每天省的最新数据
         */
        Map<String/*年月日*/, Map<String/*省*/, NcovProvDetail>> yearMonthDayProvDetailMap = new HashMap<>();
        /**
         * 按年月日存储所有市集合对象, 为了去除每天重复的省市数据， 取最新的数据
         * 目的：去重， 并留下最新的数据
         */
        Map<String/*年月日*/, Map<String/*省市*/, NcovCityDetail>> yearMonthDayCityDetailMap = new HashMap<>();
        /**
         * 所有省对象， 用于直接插入db
         */
        List<NcovProvDetail> allProvDetails = new ArrayList<>();
        /**
         * 所有省市对象， 用于直接插入db
         */
        List<NcovCityDetail> allCityDetails = new ArrayList<>();
        int allCount = ncovCityDetails.size();
        int duplicateCount = 0;
        Iterator<NcovCityDetail> it = ncovCityDetails.iterator();
        while (it.hasNext()) {
            NcovCityDetail ncovCityDetail = it.next();
               /*
                取出当天每个城市的数据（由于每个城市当天会有多条数据， 取最新的数据即可）， 具体算法如下：
                如果年月日相同， 则取出 ncovCityDetailMap
                    如果 ncovCityDetailMap 包含当前省市
                        如果更新时间<当前实体的更新时间
                            更新 ncovCityDetailMap
                            更新 ncovMap
                        否则，移除该条数据
                    如果不包含，则新增

                 */
            String province = ncovCityDetail.getProvinceName();
            String city = ncovCityDetail.getCityName();
            Date updateTime = ncovCityDetail.getUpdateTime();
            String yearMonthDay = sdf3.format(updateTime);
            String provCity = province + city;

            //处理市
            Map<String/*省市*/, NcovCityDetail> memCityDetailMap = new HashMap<>();
            //如果年月日相同, 则相同的省市只能取一个最新的
            if (yearMonthDayCityDetailMap.containsKey(yearMonthDay)) {
                memCityDetailMap = yearMonthDayCityDetailMap.get(yearMonthDay);
                //如果包含省市， 则判断省市的更新时间是否小于当前对象的时间
                if (memCityDetailMap.containsKey(provCity)) {
                    NcovCityDetail memCityDetail = memCityDetailMap.get(provCity);
                    if (memCityDetail.getUpdateTime().getTime() < updateTime.getTime()) {
                        memCityDetail = ncovCityDetail;
                    } else {
                        duplicateCount++;
                        if (log.isDebugEnabled()) {
                            log.debug("忽略，某天【{}】， 某个省市【{}】，为重复条目， 对象为{}", yearMonthDay, provCity, JSON.toJSONString(ncovCityDetail));
                        }
                        it.remove();
                    }

                } else {//否则， 新增
                    memCityDetailMap.put(provCity, ncovCityDetail);
                }

            } else {
                memCityDetailMap.put(provCity, ncovCityDetail);
            }
            yearMonthDayCityDetailMap.put(yearMonthDay, memCityDetailMap);
            //添加所有的省市对象
            allCityDetails.add(ncovCityDetail);


            //处理省
            Map<String/*省*/, NcovProvDetail> memProvDetailMap = new HashMap<>();
            if (yearMonthDayProvDetailMap.containsKey(yearMonthDay)) {
                memProvDetailMap = yearMonthDayProvDetailMap.get(yearMonthDay);
                //如果包含省， 则判断省的更新时间是否小于当前对象的时间
                if (memProvDetailMap.containsKey(province)) {
                    NcovProvDetail provDetail = memProvDetailMap.get(province);
                    if (provDetail.getUpdateTime().getTime() < updateTime.getTime()) {
                        provDetail = simpleProvDetailBuilder(ncovCityDetail);
                    } else {
                        //忽略该条数据
                    }
                } else {
                    memProvDetailMap.put(province, simpleProvDetailBuilder(ncovCityDetail));
                    allProvDetails.add(simpleProvDetailBuilder(ncovCityDetail));
                }

            } else {
                memProvDetailMap.put(province, simpleProvDetailBuilder(ncovCityDetail));
                allProvDetails.add(simpleProvDetailBuilder(ncovCityDetail));
            }
            yearMonthDayProvDetailMap.put(yearMonthDay, memProvDetailMap);

        }

        //统一每个省的统计指标
        handleProvinceStat(allCityDetails, yearMonthDayProvDetailMap);

        log.info("==>执行[putDataInRedis], 总条数【{}】, 重复条数【{}】，去除重复数据之后实际条数【{}】, 共花费【{}】毫秒",
                allCount, duplicateCount, ncovCityDetails.size(), (System.currentTimeMillis() - start));
    }

    /**
     * 统计每天每个省的指标
     *
     * @param provCityMap
     * @param yearMonthDayProvDetailMap
     */
    private void handleProvinceStat(List<NcovCityDetail> allCityDetails,
                                    Map<String/*年月日*/, Map<String/*省*/, NcovProvDetail>> yearMonthDayProvDetailMap) {
        yearMonthDayProvDetailMap.forEach((yearMonthDay, provDetailMap) -> {
            provDetailMap.forEach((province, provDetail) -> {
                Long confirmedCount = 0L;
                Long suspectedCount = 0L;
                Long curedCount = 0L;
                Long deadCount = 0L;
                List<NcovCityDetail> cities = provDetail.getCities();
                if (cities == null) {
                    cities = new ArrayList<>();
                }
                for (NcovCityDetail cityDetail : allCityDetails) {
                    String tmpProvince = cityDetail.getProvinceName();
                    Date updateTime = cityDetail.getUpdateTime();
                    String tmpYearMonthDay = sdf3.format(updateTime);
                    //如果是当天， 并且是同省，并且当前省下面不包含该对象，则添加
                    if (tmpYearMonthDay.equals(yearMonthDay)
                            && tmpProvince.equals(province)
                            && !cities.contains(cityDetail)) {
                        confirmedCount += cityDetail.getCityConfirmedCount();
                        suspectedCount += cityDetail.getCitySuspectedCount();
                        curedCount += cityDetail.getCityCuredCount();
                        deadCount += cityDetail.getCityDeadCount();
                        provDetail.setConfirmedCount(confirmedCount);
                        provDetail.setSuspectedCount(suspectedCount);
                        provDetail.setConfirmedCount(confirmedCount);
                        provDetail.setCuredCount(curedCount);
                        cities.add(cityDetail);
                        provDetail.setCities(cities);
                        provDetailMap.put(province, provDetail);
                    }
                }
                ;
            });
            //按每天的省放入对象
            redisService.put(GITHUBU_DATA_CITY_BY_DAY_REDIS_KEY+yearMonthDay,provDetailMap);
        });
    }


    /**
     * 构建省对应的市， 用于查找省下面的市的一些数据统计， 如确诊人数、治愈人数等
     *
     * @param provCityMap
     * @param ncovCityDetail
     */
    private void provCitiesMapBuilder(Map<String/*省*/, List<String>/*市*/> provCityMap,
                                      NcovCityDetail ncovCityDetail) {
        String provName = ncovCityDetail.getProvinceName();
        List<String> provCitys = provCityMap.get(provName);
        if (provCitys == null || provCitys.size() == 0) {
            provCitys = new ArrayList<>();
        }
        if (!provCitys.contains(ncovCityDetail.getCityName())) {
            provCitys.add(ncovCityDetail.getCityName());
            provCityMap.put(provName, provCitys);
        }
    }

    private NcovProvDetail simpleProvDetailBuilder(NcovCityDetail ncovCityDetail) {
        NcovProvDetail provDetail = new NcovProvDetail();
        provDetail.setProvinceName(ncovCityDetail.getProvinceName());
        provDetail.setUpdateTime(ncovCityDetail.getUpdateTime());
        return provDetail;
    }


    @Override
    public void batchUpdate(List ncovCityDetails) {

        Long start = System.currentTimeMillis();
//        Map<String/*年月日*/, Map<String/*省市*/, JSONObject/*省市*/>> ncovMap = (Map<String/*年月日*/, Map<String/*省市*/, JSONObject/*省市*/>>)redisService.get(GITHUBU_DATA_REDIS_KEY);
        //插入次数，到99
        int insertcount = 0;
        //执行sql次数
        int executeSqlNum = 0;
        //所有数量
        int allCount = 0;
        //单层遍历次数
        int travelCount = 0;
        StringBuilder sql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_PREFIX);
        StringBuilder valueSql = new StringBuilder(1024 * 500);
        List<NcovCityDetail> addrDetails = (List<NcovCityDetail>) ncovCityDetails;
        for (NcovCityDetail detail : addrDetails) {
            valueSql.append("(")
                    .append("'").append(detail.getProvinceName()).append("'").append(",")
                    .append("'").append(detail.getCityName()).append("'").append(",")
                    .append(detail.getProvCurConfirmCount()).append(",")
                    .append(detail.getProvinceConfirmedCount()).append(",")
                    .append(detail.getProvinceSuspectedCount()).append(",")
                    .append(detail.getProvinceCuredCount()).append(",")
                    .append(detail.getProvinceDeadCount()).append(",")
                    .append(detail.getCityCurConfirmCount()).append(",")
                    .append(detail.getCityConfirmedCount()).append(",")
                    .append(detail.getCitySuspectedCount()).append(",")
                    .append(detail.getCityCuredCount()).append(",")
                    .append(detail.getCityDeadCount()).append(",")
                    .append("'").append(sdf2.format(detail.getUpdateTime())).append("'")
                    .append(")");

            if (insertcount == 99) {
                valueSql.append(";");
                try {
//                DsUtil.execute(sql.append(valueSql).toString(),null);
                    jdbcTemplate.execute(sql.append(valueSql).toString());
                }catch (DataAccessException e){
                    log.error("==>[batchUpdate] occurs error. sql = {}",sql,e);
                    throw new RuntimeException(e);
                }
                insertcount = 0;
                executeSqlNum++;
                //清空
                valueSql = new StringBuilder(1024 * 50);
                sql = new StringBuilder(1024 * 500);
                sql.append(INSERT_NCOV_SQL_PREFIX);
            } else {
                valueSql.append(",");
                insertcount++;
            }

            travelCount++;

            if (travelCount == addrDetails.size() && valueSql.length() != 0) {
                String s = valueSql.substring(0, valueSql.length() - 1);
//                DsUtil.execute(sql.append(s).toString(),null);
                jdbcTemplate.execute(sql.append(s).toString());
                executeSqlNum++;
            }
        }

        log.info("执行sql【{}】次，总共数量【{}】, 执行数据库总花费【{}】毫秒"
                , executeSqlNum, travelCount, (System.currentTimeMillis() - start));
    }

    public void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
        valueSql = new StringBuilder(1024 * 50);
        sql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_PREFIX);
    }

}
