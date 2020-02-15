package com.tree.ncov.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.github.entity.NcovCityDetail;
import com.tree.ncov.github.entity.NcovCountryResult;
import com.tree.ncov.github.entity.NcovProvDetail;
import com.tree.ncov.redis.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tree.ncov.constant.Constants.*;
import static com.tree.ncov.constant.Constants.INSERT_NCOV_SQL_PREFIX;

/**
 * @ClassName com.tree.ncov.service
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
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

    @Autowired
    private RedisService redisService;

    /**
     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
     */
    static Map<String, NcovCityDetail> addrDetailMap = new HashMap<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");

    //

    /**
     * 如果不是当天数据， 忽略
     * 拿当天数据做对比， 如果存在， 则比较更新时间，如果不存在， 则添加
     *
     * @throws Exception
     */
    @Scheduled
    @Override
    public void compareAndUpdate() throws Exception {
        List<NcovProvDetail> remoteChinaProvDetails = readFileFromRemote();

        handleRemoteData(remoteChinaProvDetails);
        /*
         * 按年月日存储所有省集合对象， 取每天省的最新数据
         *
         * 算法：
         *     如果redis中没有包含年月日，则增加省，增加城市， 并更新redis
         *     否则远程的 省份更新时间 > redis中省份的更新时间， 则更新
         */

        //获取redis数据
//        Map<String/*年月日*/, Map<String/*省*/, JSONObject/*NcovProvDetail*/>> yearMonthDayProvDetailMap
//                = (Map<String, Map<String, JSONObject>> )redisService.get(GITHUBU_DATA_REDIS_KEY);

        remoteChinaProvDetails.forEach(remoteProvDetail -> {
            String province = remoteProvDetail.getProvinceName();
            Date updateTime = remoteProvDetail.getUpdateTime();
            String yearMonthDay = sdf3.format(updateTime);
            //当天对象
            Map<String/*省*/, JSONObject/*NcovProvDetail*/> curDayprovDetailMap =
                    (Map<String, JSONObject> )redisService.get(GITHUBU_DATA_CITY_BY_DAY_REDIS_KEY+":"+yearMonthDay);

            //如果当天数据为空，则新增
            if(curDayprovDetailMap == null || curDayprovDetailMap.size() == 0){
                //新增
                batchUpdate(remoteProvDetail.getCities());
                //TODO 增加省
                curDayprovDetailMap.put(province,JSON.parseObject(JSON.toJSONString(remoteProvDetail)));
                redisService.put(yearMonthDay,curDayprovDetailMap);
            }

            //如果当前数据不为空， 且update时间 > redis中时间，则更新
            JSONObject jsonObject = curDayprovDetailMap.get(province);
            NcovProvDetail redisProvDetail = JSON.toJavaObject(jsonObject, NcovProvDetail.class);
            if(updateTime.getTime() > redisProvDetail.getUpdateTime().getTime()){
                //更新
                List<NcovCityDetail> remoteCities = remoteProvDetail.getCities();
                //TODO 更新省

                //删除当天的这个省份的所有数据，

                String mysqlDeleteSql = "delete from ncov_detail where provinceName='"+province+"', TO_DAYS(updateTime) = TO_DAYS('"+sdf2.format(updateTime)+"')";
                String pgDeleteSql = "delete FROM ncov_detail WHERE finish_time BETWEEN TIMESTAMP'"+yearMonthDay+"' AND TIMESTAMP'"+yearMonthDay+" 23:59:59'";
                DsUtil.execute("mysql".equals(dsName)? mysqlDeleteSql: pgDeleteSql, null);
                //插入当前的这个省份的所有数据
                batchUpdate(remoteCities);
            }

        });

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
                cityDetail.setProvinceConfirmedCount(provDetail.getConfirmedCount());
                cityDetail.setCitySuspectedCount(provDetail.getSuspectedCount());
                cityDetail.setCityDeadCount(provDetail.getDeadCount());
                cityDetail.setCityCuredCount(provDetail.getCuredCount());
                cityDetail.setUpdateTime(updateTime);
                cityDetail.setProvinceName(provDetail.getProvinceName());
            });
        });
    }

    @Override
    public List readFileFromLocal() throws IOException {
        long start = System.currentTimeMillis();
        FileReader fileReader = new FileReader(new File(GITHUB_DATA_CSV_FILE_PATH));
        BufferedReader br = new BufferedReader(fileReader);

        String line = null;
        int i = 0;
        NcovCityDetail detail = null;
        String province = null;
        String city = null;
        List<NcovCityDetail> ncovCityDetails = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            //第一行为表头， 忽略
            if (i == 0) {
                i++;
                continue;
            }
            String[] data = line.split(",");
            detail = new NcovCityDetail();
            province = data[0];
            city = data[1];
            detail.setProvinceName(province);
            detail.setCityName(data[1]);
            detail.setProvinceConfirmedCount(Long.valueOf(data[2]));
            detail.setProvinceSuspectedCount(Long.valueOf(data[3]));
            detail.setProvinceCuredCount(Long.valueOf(data[4]));
            detail.setProvinceDeadCount(Long.valueOf(data[5]));

            detail.setCityConfirmedCount(Long.valueOf(data[6]));
            detail.setCitySuspectedCount(Long.valueOf(data[7]));
            detail.setCityCuredCount(Long.valueOf(data[8]));
            detail.setCityDeadCount(Long.valueOf(data[9]));
            Date updateTime = null;
            try {
                updateTime = sdf.parse(data[10]);
            } catch (Exception e) {
                try {
                    updateTime = sdf2.parse(data[10]);
                } catch (Exception e1) {
                    System.err.println(data[10] + "===" + JSON.toJSONString(detail));
                }
            }
            detail.setUpdateTime(updateTime);
            ncovCityDetails.add(detail);

            String yearMonthDay = sdf3.format(updateTime);
            String provCity = province + city;

            i++;
        }

        putDataInRedis(ncovCityDetails);
        log.info("==>执行[readFileFromLocal] 总花费时间：{}", (System.currentTimeMillis() - start));

        return ncovCityDetails;
    }

    @Override
    public List readFileFromRemote() throws IOException {
        long start = System.currentTimeMillis();
        FileReader fileReader = new FileReader(new File(GITHUB_DATA_JSON_FILE_PATH));
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
            if (ncovProvDetail.getCountry().indexOf("中国") != -1) {
                chinaProvDetails.add(ncovProvDetail);
                chinaProvCityMap.put(ncovProvDetail.getProvinceName(), ncovProvDetail.getCities());
            }
        });


        log.info("==>执行[readFileFromRemote] 总花费时间：{}", (System.currentTimeMillis() - start));
        return chinaProvDetails;
    }


    @Override
    public void initTable() {
        DsUtil.execute(TRUNCATE_DETAIL_TABLE,null);
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


//        redisService.put(GITHUBU_DATA_REDIS_KEY, yearMonthDayProvDetailMap);
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
//            Map<String/*省*/, NcovProvDetail> yearMonthDayProvDetail = yearMonthDayProvDetailMap.get(yearMonthDay);
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
            redisService.put(GITHUBU_DATA_CITY_BY_DAY_REDIS_KEY+":"+yearMonthDay,provDetailMap);
        });

//        System.out.println(JSON.toJSONString(yearMonthDayProvDetailMap,true));

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
                    .append(detail.getProvinceConfirmedCount()).append(",")
                    .append(detail.getProvinceSuspectedCount()).append(",")
                    .append(detail.getProvinceCuredCount()).append(",")
                    .append(detail.getProvinceDeadCount()).append(",")
                    .append(detail.getCityConfirmedCount()).append(",")
                    .append(detail.getCitySuspectedCount()).append(",")
                    .append(detail.getCityCuredCount()).append(",")
                    .append(detail.getCityDeadCount()).append(",")
                    .append("'").append(sdf2.format(detail.getUpdateTime())).append("'")
                    .append(")");

            if (insertcount == 99) {
                valueSql.append(";");
                DsUtil.execute(sql.append(valueSql).toString(),null);

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
                DsUtil.execute(sql.append(s).toString(),null);
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
