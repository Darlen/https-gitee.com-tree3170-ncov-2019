package com.tree.ncov.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.github.entity.NcovCityDetail;
import com.tree.ncov.github.entity.NcovCountry;
import com.tree.ncov.github.entity.NcovProvDetail;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.sql.PreparedStatement;
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
    public void initDataFromRemote() throws Exception {
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
                (Map<String, JSONObject>) redisService.get(GITHUBU_DATA_PROV_CITY_BY_DAY_REDIS_KEY
                        + sdf3.format(remoteChinaProvDetails.get(0).getUpdateTime()));
        /*
         * 按年月日存储所有省集合对象， 取每天省的最新数据
         *
         * 算法：
         *     如果redis中没有包含年月日，则增加省，增加城市， 并更新redis
         *     否则远程的 省份更新时间 > redis中省份的更新时间， 则更新
         */
        remoteChinaProvDetails.forEach(remoteProvDetail -> {
            //3. 处理
            handleCompareResult(remoteProvDetail, curDayProvDetailMap);

        });

    }

    /**
     * 处理比较后的结果
     * 算法：按年月日存储所有省集合对象， 取每天省的最新数据
     * 如果redis中没有包含年月日，则增加省，增加城市， 并更新redis
     * 否则远程的 省份更新时间 > redis中省份的更新时间， 则更新
     *
     * @param remoteProvDetail
     * @param curDayProvDetailMap
     */
    private void handleCompareResult(NcovProvDetail remoteProvDetail,
                                     Map<String/*省*/, JSONObject/*NcovProvDetail*/> curDayProvDetailMap) {
        Date updateTime = remoteProvDetail.getUpdateTime();
        String yearMonthDay = sdf3.format(updateTime);
        String province = remoteProvDetail.getProvinceName();
        //如果当天数据为空，则新增
        if (curDayProvDetailMap == null || curDayProvDetailMap.size() == 0) {
            //新增
            batchUpdate(remoteProvDetail.getCities());
            //TODO 增加省
            curDayProvDetailMap.put(province, JSON.parseObject(JSON.toJSONString(remoteProvDetail)));
            redisService.put(GITHUBU_DATA_PROV_CITY_BY_DAY_REDIS_KEY + yearMonthDay, curDayProvDetailMap);
        }

        //如果当前数据不为空， 且update时间 > redis中时间，则更新
        JSONObject jsonObject = curDayProvDetailMap.get(province);
        NcovProvDetail redisProvDetail = JSON.toJavaObject(jsonObject, NcovProvDetail.class);
        if (updateTime.getTime() > redisProvDetail.getUpdateTime().getTime()) {
            //更新
            List<NcovCityDetail> remoteCities = remoteProvDetail.getCities();
            //TODO 更新省

            //删除当天的这个省份的所有数据，
            String mysqlDeleteSql = "DELETE FROM NCOV_DETAIL WHERE provinceName='" + province + "', TO_DAYS(updateTime) = TO_DAYS('" + sdf2.format(updateTime) + "')";
            String pgDeleteSql = "DELETE FROM NCOV_DETAIL WHERE finish_time BETWEEN TIMESTAMP'" + yearMonthDay + "' AND TIMESTAMP'" + yearMonthDay + " 23:59:59'";
            jdbcTemplate.execute("mysql".equals(dsName) ? mysqlDeleteSql : pgDeleteSql);
            //插入当前的这个省份的所有数据
            batchUpdate(remoteCities);
        }
    }


    /**
     * 把省份相关的字段塞进城市中
     *
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
        log.info("==>[readFileFromLocal], 读取本地CSV文件:{}", localCsvUrl);
        //issue : 由于csv有的字段中包含逗号，正常读取每行已经无法用逗号分隔来计算 故引入opencsv
        try (CSVReader csvReader = new CSVReaderBuilder(new BufferedReader
                //GITHUB_DATA_CSV_FILE_PATH
                (new InputStreamReader(new FileInputStream(new File(localCsvUrl)), "UTF-8")))
                .build()) {
            Iterator<String[]> iterator = csvReader.iterator();
            while (iterator.hasNext()) {
                /*
                provinceName	provinceEnglishName	cityName	cityEnglishName
                province_confirmedCount	province_suspectedCount	province_curedCount	province_deadCount
                city_confirmedCount	city_suspectedCount	city_curedCount	city_deadCount	updateTime

                provinceName	provinceEnglishName	cityName	cityEnglishName
                province_confirmedCount	province_suspectedCount	province_curedCount	province_deadCount	city_confirmedCount	city_suspectedCount	city_curedCount	city_deadCount	updateTime
                 */
                String[] data = iterator.next();
                //第一行为表头， 忽略
                if (i == 0) {
                    i++;
                    continue;
                }
                detail = new NcovCityDetail();
                province = data[0];
                city = data[3];
                detail.setProvinceName(province);
                detail.setCityName(city);
                try {
                    detail.setProvinceConfirmedCount(Long.valueOf(data[6]));
                    detail.setProvinceSuspectedCount(Long.valueOf(data[7]));
                    detail.setProvinceCuredCount(Long.valueOf(data[8]));
                    detail.setProvinceDeadCount(Long.valueOf(data[9]));

                    detail.setCityConfirmedCount(Long.valueOf(data[10]));
                    detail.setCitySuspectedCount(Long.valueOf(data[11]));
                    detail.setCityCuredCount(Long.valueOf(data[12]));
                    detail.setCityDeadCount(Long.valueOf(data[13]));
                } catch (NumberFormatException e) {
                    System.err.println("===line" + line);
                    throw e;
                }

                Date updateTime = null;
                try {
                    updateTime = sdf.parse(data[14]);
                } catch (Exception e) {
                    try {
                        updateTime = sdf2.parse(data[14]);
                    } catch (Exception e1) {
                        System.err.println(data[14] + "===" + JSON.toJSONString(detail));
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
     *
     * @return
     * @throws IOException
     */
    @Override
    public List readFileFromRemote() throws IOException {
        long start = System.currentTimeMillis();
        log.info("==>[readFileFromRemote], 读取远程JSON文件， 注意由于github网络不通，从本地文件读取:{}", localJsonUrl);
        //注意：由于github不通， 只能到本地读取json文件
        FileReader fileReader = new FileReader(new File(localJsonUrl));
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        StringBuilder sb = new StringBuilder(1024 * 50);
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        NcovCountry result = JSON.parseObject(sb.toString(), NcovCountry.class);
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
        if (truncate) {
            jdbcTemplate.execute(TRUNCATE_DETAIL_TABLE);
            jdbcTemplate.execute("truncate `ncov_country_stat_latest`");
            jdbcTemplate.execute("truncate `ncov_country_stat`");
            jdbcTemplate.execute("truncate `ncov_province_stat_latest`");
            jdbcTemplate.execute("truncate `ncov_province_stat`");
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

        //统一所有指标
        handleStatData(allCityDetails, yearMonthDayProvDetailMap);

        log.info("==>执行[putDataInRedis], 总条数【{}】, 重复条数【{}】，去除重复数据之后实际条数【{}】, 共花费【{}】毫秒",
                allCount, duplicateCount, ncovCityDetails.size(), (System.currentTimeMillis() - start));
    }

    /**
     * 统计每天所有指标
     *
     * 先统计省份数据
     * 再统计国家数据
     *
     * @param provCityMap
     * @param yearMonthDayProvDetailMap
     */
    private void handleStatData(List<NcovCityDetail> allCityDetails,
                                Map<String/*年月日*/, Map<String/*省*/, NcovProvDetail>> yearMonthDayProvDetailMap) {
        yearMonthDayProvDetailMap.forEach((yearMonthDay, provDetailMap) -> {
            provDetailMap.forEach((province, provDetail) -> {
                //统计每天城市数据
                handleCityStatDataByDay(yearMonthDay, allCityDetails, provDetail, provDetailMap);
            });
            //统计每天省的数据
            handleProvStatDataByDay(yearMonthDay,provDetailMap);

        });
        //有多少天
        List<String> howManyDays = new ArrayList<>();
        List<NcovCountry>  countryList = new ArrayList<>();
        yearMonthDayProvDetailMap.forEach((yearMonthDay, provDetailMap) -> {
            howManyDays.add(yearMonthDay);
            //统计每天国家内省份数据数据
            //***注意， 由于省份很多数据在city里， 没有被更新， 故需要先更新省份数据，再统计国家数据
            countryList.add(handleChinaStatDataByDay(yearMonthDay,provDetailMap));
        });


        //放入当天数据， 如国家、省份
        String curDay = sdf3.format(new Date());
        redisService.put(GITHUBU_DATA_COUNTRY_CURRENT_BY_DAY_REDIS_KEY,
                redisService.get(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+curDay));
        log.info("==>当天国家统计，[{}]天中国统计【{}】",curDay,redisService.get(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+curDay));

        redisService.put(GITHUBU_DATA_PPROVINCE_CURRENT_BY_DAY_REDIS_KEY,
                redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+curDay));
        log.info("==>当天省份统计指标，[{}]天所有的省份统计【{}】",curDay,redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+curDay));

        redisService.put(GITHUBU_DATA_DAYS,howManyDays);
    }



    /**
     * 统计国家
     * @param provDetailMap
     */
    private NcovCountry handleChinaStatDataByDay(String yearMonthDay,
                                                       Map<String/*省*/, NcovProvDetail> provDetailMap) {
                Long confirmedCount = 0L;
        Long suspectedCount = 0L;
        Long curedCount = 0L;
        Long deadCount = 0L;
        Long curConfirmCount=0L;
        String countryName = "";
        Date date = null;
        for(Map.Entry<String/*省*/, NcovProvDetail> entry : provDetailMap.entrySet()){
            NcovProvDetail provDetail = entry.getValue();
            curConfirmCount += provDetail.getCurConfirmCount();
            confirmedCount += provDetail.getConfirmedCount();
            suspectedCount += provDetail.getSuspectedCount();
            curedCount += provDetail.getCuredCount();
            deadCount += provDetail.getDeadCount();
            countryName = provDetail.getCountry();
            date = provDetail.getUpdateTime();
        }
        NcovCountry country = new NcovCountry();
        country.setCurConfirmCount(curConfirmCount);
        country.setConfirmedCount(confirmedCount);
        country.setSuspectedCount(suspectedCount);
        country.setCuredCount(curedCount);
        country.setDeadCount(deadCount);
        country.setCountry(countryName);
        country.setUpdateTime(date);
        redisService.put(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+yearMonthDay, country);
        log.info("==>按每天国家统计集合，country={}",JSON.toJSONString(country));
        return country;


    }

    private void handleProvStatDataByDay(String yearMonthDay, Map<String, NcovProvDetail> provDetailMap) {
        Long confirmedCount = 0L;
        Long suspectedCount = 0L;
        Long curedCount = 0L;
        Long deadCount = 0L;
        Long curConfirmCount=0L;
        List<NcovProvDetail> list =  new ArrayList<>();
        for(Map.Entry<String, NcovProvDetail> entry : provDetailMap.entrySet()){
            NcovProvDetail provDetail = entry.getValue();
            NcovProvDetail provDetailNew = new NcovProvDetail();
            provDetailNew.setCurConfirmCount(provDetail.getCurConfirmCount());
            provDetailNew.setConfirmedCount(provDetail.getConfirmedCount());
            provDetailNew.setSuspectedCount(provDetail.getSuspectedCount());
            provDetailNew.setCuredCount(provDetail.getCuredCount());
            provDetailNew.setDeadCount(provDetail.getDeadCount());
            provDetailNew.setUpdateTime(provDetail.getUpdateTime());
            provDetailNew.setProvinceName(provDetail.getProvinceName());
            provDetailNew.setCountry(provDetail.getCountry());
            provDetail.setDeadCount(provDetail.getDeadCount());

            list.add(provDetailNew);
        }

        //按每天省份统计集合
        redisService.put(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+yearMonthDay, list);
    }

    /**
     * 统计省指标
     * @param yearMonthDay
     * @param allCityDetails
     * @param provDetail
     * @param provDetailMap
     */
    private void handleCityStatDataByDay(String yearMonthDay, List<NcovCityDetail> allCityDetails,
                                         NcovProvDetail provDetail, Map<String/*省*/, NcovProvDetail> provDetailMap) {
        Long curConfirmCount=0L;
        String province = provDetail.getProvinceName();
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
                    && tmpProvince.equals(provDetail.getProvinceName())
                    && !cities.contains(cityDetail)) {
                provDetail.setCurConfirmCount(cityDetail.getProvCurConfirmCount());
                provDetail.setConfirmedCount(cityDetail.getProvinceConfirmedCount());
                provDetail.setSuspectedCount(cityDetail.getProvinceSuspectedCount());
                provDetail.setCuredCount(cityDetail.getProvinceCuredCount());
                provDetail.setDeadCount(cityDetail.getProvinceDeadCount());
                cities.add(cityDetail);
                provDetail.setCities(cities);
                provDetailMap.put(province, provDetail);
            }
        }
        //按每天的省-市放入对象
        redisService.put(GITHUBU_DATA_PROV_CITY_BY_DAY_REDIS_KEY + yearMonthDay, provDetailMap);
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
        //插入detail表
        batchInsertDetail(ncovCityDetails);
        batchInsertCountry();
        batchInsertCountryLatest();
        batchInsertProvince();
        batchInsertProvinceLatest();
    }

    private void batchInsertProvinceLatest() {
        JSONArray array = (JSONArray) redisService.get(GITHUBU_DATA_PPROVINCE_CURRENT_BY_DAY_REDIS_KEY);
        if(array != null) {//有可能第二天， redis取不到数据
            List<NcovProvDetail> list = (List<NcovProvDetail>) array.toJavaList(NcovProvDetail.class);
            executeInsertProvice(list, "ncov_province_stat_latest");
        }
    }

    private void batchInsertCountryLatest() {
        JSONObject object = (JSONObject) redisService.get(GITHUBU_DATA_COUNTRY_CURRENT_BY_DAY_REDIS_KEY );
        if(object != null) {//有可能第二天， redis取不到数据
            NcovCountry country = object.toJavaObject(NcovCountry.class);
            executeInsertCountry(country, "ncov_country_stat_latest");
        }
    }

    private void batchInsertCountry() {
        List<String> days = (List<String>)redisService.get(GITHUBU_DATA_DAYS);
        for(String day: days) {
            JSONObject object = (JSONObject) redisService.get(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY + day);
            NcovCountry country = object.toJavaObject(NcovCountry.class);
            executeInsertCountry(country,"ncov_country_stat");
        }
    }

    private void executeInsertCountry(NcovCountry country,String table) {
        String sql =
                "INSERT INTO "+table+" (\n" +
                        "\tcountry,\n" +
                        "\tcurrent_confirm_count,\n" +
                        "\tconfirmed_count,\n" +
                        "\tsuspected_count,\n" +
                        "\tcured_count,\n" +
                        "\tdead_count,\n" +
                        "\tupdate_date\n" +
                        ")VALUES(?,?,?,?,?,?,'"+sdf3.format(country.getUpdateTime())+"')";//?)";//"
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1,country.getCountry());
                ps.setLong(2,country.getCurConfirmCount());
                ps.setLong(3,country.getConfirmedCount());
                ps.setLong(4,country.getSuspectedCount());
                ps.setLong(5,country.getCuredCount());
                ps.setLong(6,country.getDeadCount());
//                System.out.println("==========="+JSON.toJSONString(sdf2.format(country.getUpdateTime())));
//                ps.setDate(7,new java.sql.Date(country.getUpdateTime().getTime()));
//                ps.setDate(7, java.sql.Date.valueOf(sdf2.format(country.getUpdateTime())));
//                new java.sql.Date()sdf2.format(country.getUpdateTime()));
            }
        });
    }

    private void batchInsertProvince() {
        List<String> days = (List<String>)redisService.get(GITHUBU_DATA_DAYS);
        for(String day: days){
            JSONArray array = (JSONArray) redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+day);
            List<NcovProvDetail> list = (List<NcovProvDetail>) array.toJavaList(NcovProvDetail.class);
            executeInsertProvice(list,"ncov_province_stat");
        }
        System.out.println(1);
    }

    private void executeInsertProvice(List<NcovProvDetail> list,String table) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO "+table+" (\n" +
                "\tcountry,\n" +
                "\tprovince,\n" +
                "\tcurrent_confirm_count,\n" +
                "\tconfirmed_count,\n" +
                "\tsuspected_count,\n" +
                "\tcured_count,\n" +
                "\tdead_count,\n" +
                "\tupdate_date\n" +
                ")VALUES";
        StringBuilder valueSql =  new StringBuilder(1024*50);
        StringBuilder finalSql =  new StringBuilder(1024*50);
        finalSql.append(sql);

        int exeCount = 0;
        for(int i = 0; i< list.size(); i++ ) {
            NcovProvDetail provDetail = list.get(i);
            valueSql.append("(")
                    .append(" '" ).append(provDetail.getCountry()).append("' " )
                    .append(", '" ).append(provDetail.getProvinceName()).append("' " )
                    .append("," + provDetail.getCurConfirmCount())
                    .append("," + provDetail.getConfirmedCount())
                    .append("," + provDetail.getSuspectedCount())
                    .append("," + provDetail.getCuredCount())
                    .append("," + provDetail.getDeadCount() )
                    .append(",'" + sdf3.format(provDetail.getUpdateTime())+"'")
                    .append(")");
            if (exeCount == 99) {
                jdbcTemplate.execute(finalSql.append(valueSql).toString());
                exeCount++;
                //初始化valuesql与finalsql
                valueSql = new StringBuilder(1024 * 50);
                finalSql = new StringBuilder(1024 * 50);
                finalSql.append(sql);

            } else {
                valueSql.append(",");
            }

        }
        if(!StringUtils.isEmpty(valueSql)){
                jdbcTemplate.execute(finalSql.append(valueSql.substring(0, valueSql.length() - 1)).toString());
            exeCount++;
        }

        log.info("==>执行[executeInsertProvice], 【{}】次，总共数量【{}】, 执行数据库总花费【{}】毫秒"
                , exeCount, list.size(), (System.currentTimeMillis() - start));
    }


    private void batchInsertDetail(List ncovCityDetails) {
        Long start = System.currentTimeMillis();
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
                    .append(detail.getProvCurConfirmCount() == null ? 0 : detail.getProvCurConfirmCount()).append(",")
                    .append(detail.getProvinceConfirmedCount()).append(",")
                    .append(detail.getProvinceSuspectedCount()).append(",")
                    .append(detail.getProvinceCuredCount()).append(",")
                    .append(detail.getProvinceDeadCount()).append(",")
                    .append(detail.getCityCurConfirmCount() == null ? 0 : detail.getCityCurConfirmCount()).append(",")
                    .append(detail.getCityConfirmedCount()).append(",")
                    .append(detail.getCitySuspectedCount()).append(",")
                    .append(detail.getCityCuredCount()).append(",")
                    .append(detail.getCityDeadCount()).append(",")
                    .append("'").append(sdf2.format(detail.getUpdateTime())).append("'")
                    .append(")");

            if (insertcount == 99) {
                valueSql.append(";");
                try {
                    jdbcTemplate.execute(sql.append(valueSql).toString());
                } catch (DataAccessException e) {
                    log.error("==>[batchUpdate] occurs error. sql = {}", sql, e);
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

        log.info("==>执行[batchInsertDetail]sql【{}】次，总共数量【{}】, 执行数据库总花费【{}】毫秒"
                , executeSqlNum, travelCount, (System.currentTimeMillis() - start));
    }


    public void insertChinaStatData() {

    }

    public void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
        valueSql = new StringBuilder(1024 * 50);
        sql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_PREFIX);
    }

}
