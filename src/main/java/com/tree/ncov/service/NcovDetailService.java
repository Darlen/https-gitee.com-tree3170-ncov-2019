package com.tree.ncov.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.tree.ncov.statdata.ProvinceDetailService;
import com.tree.ncov.statdata.dto.NcovOverallResult;
import com.tree.ncov.statdata.entity.NcovCityDetail;
import com.tree.ncov.statdata.entity.NcovCountry;
import com.tree.ncov.statdata.entity.NcovCountryLatest;
import com.tree.ncov.statdata.entity.NcovProvDetail;
import com.tree.ncov.statdata.repository.CountryLatestRepository;
import com.tree.ncov.statdata.repository.CountryRepository;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static Map<String/*完整名*/, String/*short name*/> provinceMap = new HashMap<>();
    static {

        provinceMap.put("上海市","上海");
        provinceMap.put("云南省","云南");
        provinceMap.put("内蒙古自治区","内蒙古");
        provinceMap.put("北京市","北京");
        provinceMap.put("吉林省","吉林");
        provinceMap.put("四川省","四川");
        provinceMap.put("天津市","天津");
        provinceMap.put("宁夏回族自治区","宁夏");
        provinceMap.put("安徽省","安徽");
        provinceMap.put("山东省","山东");
        provinceMap.put("山西省","山西");
        provinceMap.put("广东省","广东");
        provinceMap.put("广西壮族自治区","广西");
        provinceMap.put("新疆维吾尔自治区","新疆");
        provinceMap.put("江苏省","江苏");
        provinceMap.put("江西省","江西");
        provinceMap.put("河北省","河北");
        provinceMap.put("河南省","河南");
        provinceMap.put("浙江省","浙江");
        provinceMap.put("海南省","海南");
        provinceMap.put("湖北省","湖北");
        provinceMap.put("湖南省","湖南");
        provinceMap.put("甘肃省","甘肃");
        provinceMap.put("福建省","福建");
        provinceMap.put("贵州省","贵州");
        provinceMap.put("辽宁省","辽宁");
        provinceMap.put("重庆市","重庆");
        provinceMap.put("陕西省","陕西");
        provinceMap.put("西藏自治区","西藏");
        provinceMap.put("甘肃省","甘肃");
        provinceMap.put("青海省","青海");
        provinceMap.put("黑龙江省","黑龙江");
        provinceMap.put("澳门","澳门");
        provinceMap.put("香港","香港");
        provinceMap.put("台湾","台湾");
    }

    @Value("${ncov.ds.name:mysql}")
    private String dsName;

    @Value("${ncov.githubdata.truncateable:false}")
    boolean truncateable;

    @Value("${ncov.githubdata.from}")
    private String from;

    @Value("${ncov.githubdata.remote.area.json.url}")
    private String remoteJsonUrl;
    @Value("${ncov.githubdata.remote.overall.json.url}")
    private String overallRemoteJsonUrl;

    @Value("${ncov.githubdata.remote.area.json.filename}")
    private String remoteJsonFilename;

    @Value("${ncov.githubdata.remote.area.zip.url}")
    private String remoteZipUrl;

    @Value("${ncov.githubdata.remote.area.zip.filename}")
    private String remoteZipFilename;

    /**
     * 由于网络不通， 只能使用本地json文件
     */
    @Value("${ncov.githubdata.local.json.url}")
    private String localJsonUrl;

    @Value("${ncov.githubdata.local.json.filename}")
    private String localJsonFilename;
    @Autowired
    private RestTemplate restTemplate;

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
    @Autowired
    private ProvinceDetailService provinceDetailService;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryLatestRepository countryLatestRepository;


    /**
     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
     */
    static Map<String, NcovCityDetail> addrDetailMap = new HashMap<>();
//    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
//    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//    static SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMddHHmmss");
//    static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
//    static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd");

    /**
     * 如果不是当天数据， 忽略
     * 拿当天数据做对比， 如果存在， 则比较更新时间，如果不存在， 则添加
     *
     * @throws Exception
     */
    @Override
    protected void executeCompareAndUpdate(List list, Object obj) {
        TimeInterval timeInterval = new TimeInterval();
        List<NcovProvDetail> remoteChinaProvDetails = list;
        NcovCountry dbCountry = (NcovCountry) obj;

        List<NcovProvDetail> dbChinaProvDetails = dbCountry.getResults();
        AtomicInteger executeNum = new AtomicInteger();
        List<String> exeProvinceList = new ArrayList<>();
        List<String> notTodayProvinceList = new ArrayList<>();

        remoteChinaProvDetails.forEach(remoteProvDetail -> {
            handleCompareResult(executeNum, exeProvinceList, remoteProvDetail, dbChinaProvDetails);
        });

        //远程获取over all 的各个指标
        updateOverallData();

        log.info("==>[executeCompareAndUpdate], 总处理条数【{}】, 处理的省份为:{}, 共花费【{}】毫秒",
                executeNum, JSON.toJSONString(exeProvinceList),timeInterval.interval());

    }

    @Override
    protected Object loadTodayData() throws IOException {
        return  provinceDetailService.getTodayCountryDetailFromDB();
    }

    @Override
    public void initDataFromRemote() throws Exception {
        log.warn("*****************************");
        log.warn("#由于远程只有当天的数据，该方法[initDataFromRemote]不支持从远程初始化数据#");
        log.warn("*****************************");
    }


    /**
     * 处理全国概览数据
     */
    private void updateOverallData() {
        NcovOverallResult overall = restTemplate.getForObject(overallRemoteJsonUrl, NcovOverallResult.class);
        log.info("==>执行[updateOverallData], 读取远程JSON文件， 地址为:{}\n 内容为：{}",
                overallRemoteJsonUrl,JSON.toJSONString(overall));

        if(overall != null && overall.isSuccess() && overall.getResults() != null) {
            NcovCountry overallCountry = overall.getResults().get(0);
            overallCountry.setCountryName(CHINA);
            overallCountry.setCreateTime(new Date());
            /*
            处理当天中国数据
             */
            countryRepository.deleteToday();
            countryRepository.save(overallCountry);
            /*
            处理当天中国Latest数据
            */
            countryLatestRepository.deleteAll();
            countryLatestRepository.save(new NcovCountryLatest(overallCountry));
        }


    }

    private void handleCompareResult(AtomicInteger executeNum, List<String> exeProvinceList,
                                     NcovProvDetail remoteProvDetail,List<NcovProvDetail> dbChinaProvDetails){
        TimeInterval timeInterval = DateUtil.timer();
        String province = remoteProvDetail.getProvinceName();
        AtomicBoolean ignoreProvince = new AtomicBoolean(false);
        log.info("==>【handleCompareResult】, 对比省份【{}】开始...",province);
        Date remoteUpdateTime = remoteProvDetail.getUpdateTime();

        if(dbChinaProvDetails == null || dbChinaProvDetails.size() == 0){
            log.info("  ==>由于数据库中今天[{}]没人任何数据， 全部更新该省份[{}]包括市区数据===========",
                    DateUtil.today(),remoteProvDetail.getProvinceName());
            log.info(JSON.toJSONString(remoteProvDetail));
            //如果远程数据不是当天数据， 修改为今天日期， 并插入到今天的数据中， 因为每天都需要数据
            setRemoteTime2Today(remoteProvDetail);

            //插入城市数据
            batchInsertDetail(remoteProvDetail.getCities());

            //更新所有远程数据
            provinceDetailService.updateProvinceTodayData();

            executeNum.getAndIncrement();
            exeProvinceList.add(province);
        }else {
            //如果当前库不包含该远程数据
            if (!dbChinaProvDetails.contains(remoteProvDetail)) {
                //如果远程数据不是当天数据， 修改为今天日期， 并插入到今天的数据中
                setRemoteTime2Today(remoteProvDetail);

                log.info("  ==>由于今天[{}]的数据库中不存在该省份【{}】，直接更新该省包括市区数据===========",
                        DateUtil.today(), province);
                log.info(JSON.toJSONString(remoteProvDetail));
                //插入城市数据
                batchInsertDetail(remoteProvDetail.getCities());
                //更新所有远程数据
                provinceDetailService.updateProvinceTodayData();

                executeNum.getAndIncrement();
                exeProvinceList.add(province);
            } else { //如果包含， 那就比较更新时间， 如果远程更新时间更早， 那就更新， 否则忽略
                //遍历DB数据
                dbChinaProvDetails.forEach(dbProvDetail -> {
                    Date dbUpdateTime = dbProvDetail.getUpdateTime();
                    long dbFormateUpdateTime  = Long.valueOf(DateUtil.format(dbUpdateTime,PURE_DATETIME_PATTERN));
                    long remoteFormateUpdateTime = Long.valueOf(DateUtil.format(remoteUpdateTime,PURE_DATETIME_PATTERN));

                    //比较同一省份， 时间大小， 如果远程 > DB, 则执行删除当天再更新所有数据
                    if (province.equals(dbProvDetail.getProvinceName())
                            && remoteFormateUpdateTime > dbFormateUpdateTime) {
                        log.info("  ==>该省份【{}】在DB中的数据时间为【{}】, " +
                                "远程时间数据为【{}】, DB < REMOTE， 更新该省份所有数据===========",
                                province,dbFormateUpdateTime,remoteFormateUpdateTime);
                        String yearMonthDay = DateUtil.format(remoteUpdateTime,NORM_DATE_PATTERN);

                        //删除当天的这个省份下面的所有城市的数据，
                        String mysqlDeleteSql = "DELETE FROM NCOV_DETAIL WHERE province_name='" + province
                                + "' AND  TO_DAYS(updateTime) = TO_DAYS('" + DateUtil.format(remoteUpdateTime,NORM_DATETIME_MS_PATTERN) + "')";
                        String pgDeleteSql = "DELETE FROM NCOV_DETAIL WHERE  province_name='" + province
                                + "' AND (updateTime BETWEEN TIMESTAMP'" + yearMonthDay + "' AND TIMESTAMP'" + yearMonthDay + " 23:59:59') ";
                        jdbcTemplate.execute("mysql".equals(dsName) ? mysqlDeleteSql : pgDeleteSql);
                        batchInsertDetail(remoteProvDetail.getCities());

                        //更新所有远程数据
                        provinceDetailService.updateProvinceTodayData();
                        log.info("  ==> 更新该省份【{}】共花费[{}]毫秒 ",province, timeInterval.interval());

                        executeNum.getAndIncrement();
                        exeProvinceList.add(province);
                    } else {
                        //忽略
                        ignoreProvince.set(true);
                        log.debug("  ==> 忽略，该省份【{}】在DB中的数据时间为【{}】, " +
                                        "远程时间数据为【{}】, DB > REMOTE， 忽略===========",
                                province,dbFormateUpdateTime,remoteFormateUpdateTime);
                    }
                });
            }
        }
        if(!ignoreProvince.get()) {
            log.info("==>【handleCompareResult】结束, 共花费[{}]毫秒===========", timeInterval.interval());
        }

    }

    private void setRemoteTime2Today(NcovProvDetail remoteProvDetail) {
        Date day = DateUtil.date();
        long today = Long.valueOf(DateUtil.format(day,PURE_DATE_PATTERN));
        long remote = Long.valueOf(DateUtil.format(remoteProvDetail.getUpdateTime(),PURE_DATE_PATTERN));
        if(remote < today ){
            log.info("==>[setRemoteTime2Today]， 今天[{}]的DB不存在该省份【{}】, 但该省份的时间是【{}】, \n" +
                    "由于远程的数据时间小于今天, 故修改远程数据为今天的日期：{} " ,
                    today,remoteProvDetail.getProvinceName(), remote,today);
            remoteProvDetail.setUpdateTime(day);
            for(NcovCityDetail cityDetail:remoteProvDetail.getCities()){
                cityDetail.setUpdateTime(day);
            }
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
            String province = provDetail.getProvinceName();
            //如果没有城市， 且城市为香港、澳门、台湾， 则添加
            if((provDetail.getCities() == null || provDetail.getCities().size() == 0)
                   && (province.indexOf("香港") != -1 || province.indexOf("澳门") != -1 || province.indexOf("台湾")!=-1)){
                List<NcovCityDetail> cityDetails = new ArrayList<>();
                cityDetails.add(cityObjBuilder(new NcovCityDetail(),provDetail));;
                provDetail.setCities(cityDetails);
                provDetail.setCreateTime(DateUtil.date());
            }

            List<NcovCityDetail> cityDetails = provDetail.getCities();
            if(cityDetails != null) {
                cityDetails.forEach(cityDetail -> {
                    cityObjBuilder(cityDetail,provDetail);
                });
            }
        });
    }
    private NcovCityDetail cityObjBuilder(NcovCityDetail cityDetail , NcovProvDetail provDetail){
        cityDetail.setProvCurConfirmCount(provDetail.getCurConfirmCount());
        cityDetail.setProvinceConfirmedCount(provDetail.getConfirmedCount());
        cityDetail.setProvinceSuspectedCount(provDetail.getSuspectedCount());
        cityDetail.setProvinceDeadCount(provDetail.getDeadCount());
        cityDetail.setProvinceCuredCount(provDetail.getCuredCount());
        cityDetail.setUpdateTime(provDetail.getUpdateTime());
        cityDetail.setProvinceName(provDetail.getProvinceName());
        cityDetail.setProvinceShortName(provDetail.getProvinceShortName());
        cityDetail.setCountryName(provDetail.getCountryName());
        cityDetail.setCreateTime(DateUtil.date());

        return cityDetail;
    }

    @Override
    public List readFileFromLocal() throws IOException {
        TimeInterval timeInterval = DateUtil.timer();
        String line = null;
        int i = 0;
        NcovCityDetail detail = null;
        String province = null;
        String city = null;
        List<NcovCityDetail> ncovCityDetails = new ArrayList<>();
        log.info("==>[readFileFromLocal], 读取本地CSV文件: {}", localCsvUrl);
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
                detail.setCountryName(CHINA);
                province = data[0];
                city = data[3];
                //单独处理province字段， 由于星空画板不支持省份后面带有"省"， 比如广东省-->广东
                detail.setProvinceName(province);
                String provinceShortName = handleProviceShortNameSpecial(province);
                detail.setProvinceShortName(provinceShortName);
                detail.setCityName(city);
                try {
                    detail.setProvinceConfirmedCount(Long.valueOf(data[6]));
                    detail.setProvinceSuspectedCount(Long.valueOf(data[7]));
                    detail.setProvinceCuredCount(Long.valueOf(data[8]));
                    detail.setProvinceDeadCount(Long.valueOf(data[9]));

                    detail.setConfirmedCount(Long.valueOf(data[10]));
                    detail.setSuspectedCount(Long.valueOf(data[11]));
                    detail.setCuredCount(Long.valueOf(data[12]));
                    detail.setDeadCount(Long.valueOf(data[13]));
                } catch (NumberFormatException e) {
                    System.err.println("===line" + line);
                    throw e;
                }

                Date updateTime = null;
                try {
                    updateTime = DateUtil.parse(data[14],NORM_DATETIME_MS_PATTERN2);
                } catch (Exception e) {
                    log.debug(" ==>解析日期出错，数据为: {}, 解析格式为: {}, \n整条数据为：{}",
                            data[14],NORM_DATETIME_MS_PATTERN2,JSON.toJSONString(detail),e.getMessage());
                    try {
                        updateTime = DateUtil.parse(data[14],NORM_DATETIME_MS_PATTERN);
                    } catch (Exception e1) {
                        log.error(" ==>解析日期出错，数据为: {}, 解析格式为: {}, \n整条数据为：{}",
                                data[14],NORM_DATETIME_MS_PATTERN,JSON.toJSONString(detail),e.getMessage());
                    }
                }
                detail.setUpdateTime(updateTime);
                ncovCityDetails.add(detail);

                String yearMonthDay = DateUtil.format(updateTime,NORM_DATE_PATTERN);
                String provCity = province + city;

                i++;
            }
        }
        log.info("==>执行[readFileFromLocal] 总花费【{}】毫秒", timeInterval.interval());

        return ncovCityDetails;
    }

    /**
     * 单独处理province字段， 由于星空画板不支持省份后面带有"省"， 比如广东省-->广东
     * @param province
     * @return
     */
    private String handleProviceShortNameSpecial(String province){
        return Optional.ofNullable(provinceMap.get(province)).orElse(province);

    }

    /**
     * 远程json是一天的数据， 故该方法仅用于做更新的时候使用(compareAndUpdate)
     *
     * @return
     * @throws IOException
     */
    @Override
    public List readFileFromRemote() throws IOException {
        TimeInterval timeInterval = DateUtil.timer();
//        log.info("==>[readFileFromRemote], 读取远程JSON文件， 注意由于github网络不通，从本地文件读取:{}", localJsonUrl);
        //注意：由于github不通， 只能到本地读取json文件
//        FileReader fileReader = new FileReader(new File(localJsonUrl));
//        BufferedReader br = new BufferedReader(fileReader);
//        String line = null;
//        StringBuilder sb = new StringBuilder(1024 * 50);
//        while ((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        NcovCountry result = JSON.parseObject(sb.toString(), NcovCountry.class);
        log.info("==>[readFileFromRemote], 读取远程JSON文件， 地址为:{}", remoteJsonUrl);
        NcovCountry result = restTemplate.getForObject(remoteJsonUrl,NcovCountry.class);
        //处理数据, 获取有效的中国省市数据
        List<NcovProvDetail> provDetails = result.getResults();
        List<NcovProvDetail> chinaProvDetails = new ArrayList<>();

        provDetails.forEach(ncovProvDetail -> {
            //只获取国家为中国的数据
            if (ncovProvDetail.getCountryName().indexOf(CHINA) != -1) {
                chinaProvDetails.add(ncovProvDetail);
//                chinaProvCityMap.put(ncovProvDetail.getProvinceName(), ncovProvDetail.getCities());
            }
        });

        //把省份相关的字段塞进城市中
        handleRemoteData(chinaProvDetails);

        log.info("==>执行[readFileFromRemote] , 总条数【{}】，总花费【{}】毫秒",provDetails.size(), timeInterval.interval());
        return chinaProvDetails;

    }


    @Override
    public void initTable() {
        if (truncateable) {
            jdbcTemplate.execute(TRUNCATE_DETAIL_TABLE);
            jdbcTemplate.execute(TRUNCATE_COUNTRY_TABLE);
            jdbcTemplate.execute(TRUNCATE_COUNTRY_LATEST_TABLE);
            jdbcTemplate.execute(TRUNCATE_PROVINCE_TABLE);
            jdbcTemplate.execute(TRUNCATE_PROVINCE_LATEST_TABLE);
        }
    }


    @Override
    public void putDataInRedis(List ncovCityDetails) {
        TimeInterval timeInterval = new TimeInterval();
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
            String yearMonthDay = DateUtil.format(updateTime, NORM_DATE_PATTERN);
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
                allCount, duplicateCount, ncovCityDetails.size(), timeInterval.interval());
    }

    /**
     * 简单组装
     * @param ncovCityDetail
     * @return
     */
    private NcovProvDetail simpleProvDetailBuilder(NcovCityDetail ncovCityDetail) {
        NcovProvDetail provDetail = new NcovProvDetail();
        provDetail.setCountryName(ncovCityDetail.getCountryName());
        provDetail.setProvinceName(ncovCityDetail.getProvinceName());
        provDetail.setProvinceShortName(ncovCityDetail.getProvinceShortName());
        provDetail.setUpdateTime(ncovCityDetail.getUpdateTime());
        provDetail.setCurrentConfirmedCount(ncovCityDetail.getCurrentConfirmedCount());
        provDetail.setConfirmedCount(ncovCityDetail.getConfirmedCount());
        provDetail.setSuspectedCount(ncovCityDetail.getSuspectedCount());
        provDetail.setCuredCount(ncovCityDetail.getCuredCount());
        provDetail.setDeadCount(ncovCityDetail.getDeadCount());
        provDetail.setCreateTime(ncovCityDetail.getCreateTime());
        return provDetail;
    }

    /**
     * 统计每天所有指标
     *
     * 先统计省份数据
     * 再统计国家数据
     *
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
        String curDay = DateUtil.today();
        redisService.put(GITHUBU_DATA_COUNTRY_CURRENT_BY_DAY_REDIS_KEY,
                redisService.get(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+curDay));
        log.info("==>当天国家统计，[{}]天中国统计【{}】",curDay,redisService.get(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+curDay));

        redisService.put(GITHUBU_DATA_PPROVINCE_CURRENT_BY_DAY_REDIS_KEY,
                redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+curDay));
        log.info("==>当天省份统计指标，[{}]天所有的省份统计【{}】",curDay,redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+curDay));

        redisService.put(GITHUBU_DATA_DAYS,howManyDays);
    }



    /**
     * 统计国家相关指标数据
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
            countryName = provDetail.getCountryName();
            date = provDetail.getUpdateTime();
        }
        NcovCountry country = new NcovCountry();
        country.setCurrentConfirmedCount(curConfirmCount);
        country.setConfirmedCount(confirmedCount);
        country.setSuspectedCount(suspectedCount);
        country.setCuredCount(curedCount);
        country.setDeadCount(deadCount);
        country.setCountryName(countryName);
        country.setUpdateTime(date);
        redisService.put(GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY+yearMonthDay, country);
        log.info("==>按天【{}】统计国家，country为【{}】, 数据为: {}",yearMonthDay, country.getCountryName(), JSON.toJSONString(country));
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
            provDetailNew.setCurrentConfirmedCount(provDetail.getCurConfirmCount());
            provDetailNew.setConfirmedCount(provDetail.getConfirmedCount());
            provDetailNew.setSuspectedCount(provDetail.getSuspectedCount());
            provDetailNew.setCuredCount(provDetail.getCuredCount());
            provDetailNew.setDeadCount(provDetail.getDeadCount());
            provDetailNew.setUpdateTime(provDetail.getUpdateTime());
            provDetailNew.setProvinceName(provDetail.getProvinceName());
            provDetailNew.setProvinceShortName(provDetail.getProvinceShortName());
            provDetailNew.setCountryName(provDetail.getCountryName());
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
            String tmpYearMonthDay = DateUtil.today();
            //如果是当天， 并且是同省，并且当前省下面不包含该对象，则添加
            if (tmpYearMonthDay.equals(yearMonthDay)
                    && tmpProvince.equals(provDetail.getProvinceName())
                    && !cities.contains(cityDetail)) {
                provDetail.setCurrentConfirmedCount(cityDetail.getProvCurConfirmCount());
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




    @Override
    public void initBatchUpdate(List ncovCityDetails) {
        TimeInterval timeInterval = new TimeInterval();
        //插入detail表
        batchInsertDetail(ncovCityDetails);
        batchInsertCountry();
        batchInsertCountryLatest();
        batchInsertProvince();
        batchInsertProvinceLatest();
        log.info("==>[initBatchUpdate], 共花费【{}】毫秒", timeInterval.interval());
    }

    private void batchInsertProvinceLatest() {
        JSONArray array = (JSONArray) redisService.get(GITHUBU_DATA_PPROVINCE_CURRENT_BY_DAY_REDIS_KEY);
        if(array != null) {//有可能第二天， redis取不到数据
            List<NcovProvDetail> list = (List<NcovProvDetail>) array.toJavaList(NcovProvDetail.class);
            executeInsertProvice(list, "ncov_province_stat_latest", DateUtil.today());
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
                        ")VALUES(?,?,?,?,?,?,'"+DateUtil.format(country.getUpdateTime(),NORM_DATE_PATTERN)+"')";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1,Optional.ofNullable(country.getCountryName()).orElse(CHINA));
                ps.setLong(2,country.getCurConfirmCount());
                ps.setLong(3,country.getConfirmedCount());
                ps.setLong(4,country.getSuspectedCount());
                ps.setLong(5,country.getCuredCount());
                ps.setLong(6,country.getDeadCount());
            }
        });
    }

    private void batchInsertProvince() {
        List<String> days = (List<String>)redisService.get(GITHUBU_DATA_DAYS);
        for(String day: days){
            insertProvince(day);
        }
    }

    private void insertProvince(String day){
        JSONArray array = (JSONArray) redisService.get(GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY+day);
        List<NcovProvDetail> list = (List<NcovProvDetail>) array.toJavaList(NcovProvDetail.class);
        executeInsertProvice(list,"ncov_province_stat",day);

    }

    private void executeInsertProvice(List<NcovProvDetail> list,String table,String day) {
        TimeInterval timeInterval = new TimeInterval();
        String sql = "INSERT INTO "+table+" (\n" +
                "\tcountry,\n" +
                "\tprovince,\n" +
                "\tprovince_short_name,\n" +
                "\tcurrent_confirm_count,\n" +
                "\tconfirmed_count,\n" +
                "\tsuspected_count,\n" +
                "\tcured_count,\n" +
                "\tdead_count,\n" +
                "\tupdate_date,\n" +
                "\tcreatetime\n" +
                ")VALUES";
        StringBuilder valueSql =  new StringBuilder(1024*50);
        StringBuilder finalSql =  new StringBuilder(1024*50);
        finalSql.append(sql);

        int exeCount = 0;
        for(int i = 0; i< list.size(); i++ ) {
            NcovProvDetail provDetail = list.get(i);
            valueSql.append("(")
                    .append(" '" ).append(Optional.ofNullable(provDetail.getCountryName()).orElse(CHINA)).append("' " )
                    .append(", '" ).append(provDetail.getProvinceName()).append("' " )
                    .append(", '" ).append(provDetail.getProvinceShortName()).append("' " )
                    .append("," + provDetail.getCurConfirmCount())
                    .append("," + provDetail.getConfirmedCount())
                    .append("," + provDetail.getSuspectedCount())
                    .append("," + provDetail.getCuredCount())
                    .append("," + provDetail.getDeadCount() )
                    .append(",'" + DateUtil.format(provDetail.getUpdateTime(),NORM_DATE_PATTERN)+"'")
                    .append(",'" + DateUtil.format(provDetail.getCreateTime(),NORM_DATETIME_MS_PATTERN)+"'")
                    .append(")");
            //按每100执行一次
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

        log.info("==>[executeInsertProvice], 执行插入【{}】天数据【{}】次，总共数量【{}】, 执行数据库总花费【{}】毫秒",
                day, exeCount, list.size(), timeInterval.interval());
    }


    private void batchInsertDetail(List ncovCityDetails) {
        TimeInterval timeInterval = new TimeInterval();
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
                    .append("'").append(detail.getCountryName()).append("'").append(",")
                    .append("'").append(detail.getProvinceName()).append("'").append(",")
                    .append("'").append(detail.getProvinceShortName()).append("'").append(",")
                    .append("'").append(detail.getCityName()).append("'").append(",")
                    .append(detail.getProvCurConfirmCount() == null ? 0 : detail.getProvCurConfirmCount()).append(",")
                    .append(detail.getProvinceConfirmedCount()).append(",")
                    .append(detail.getProvinceSuspectedCount()).append(",")
                    .append(detail.getProvinceCuredCount()).append(",")
                    .append(detail.getProvinceDeadCount()).append(",")
                    .append(detail.getCurConfirmCount() == null ? 0 : detail.getCurConfirmCount()).append(",")
                    .append(detail.getConfirmedCount()).append(",")
                    .append(detail.getSuspectedCount()).append(",")
                    .append(detail.getCuredCount()).append(",")
                    .append(detail.getDeadCount()).append(",")
                    .append("'").append(DateUtil.format(detail.getUpdateTime(),NORM_DATETIME_MS_PATTERN)).append("'")
                    .append(")");

            if (insertcount == 99) {
                valueSql.append(";");
                try {
                    jdbcTemplate.execute(sql.append(valueSql).toString());
                } catch (DataAccessException e) {
                    log.error("==>[initBatchUpdate] 出错. sql = {}", sql, e);
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
                jdbcTemplate.execute(sql.append(s).toString());
                executeSqlNum++;
            }
        }

        log.info("==>[batchInsertDetail]，执行sql【{}】次，总共数量【{}】, 执行数据库总花费【{}】毫秒"
                , executeSqlNum, travelCount, timeInterval.interval());
    }

    public void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
        valueSql = new StringBuilder(1024 * 50);
        sql = new StringBuilder(1024 * 500);
        sql.append(INSERT_NCOV_SQL_PREFIX);
    }

}
