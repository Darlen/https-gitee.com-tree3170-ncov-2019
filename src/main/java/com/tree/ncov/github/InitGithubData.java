//package com.tree.ncov.github;
//
//import com.alibaba.fastjson.JSON;
//import com.tree.ncov.Util.DsUtil;
//import com.tree.ncov.github.entity.NcovCountryResult;
//import com.tree.ncov.github.entity.NcovCityDetail;
//import com.tree.ncov.github.entity.NcovProvDetail;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static com.tree.ncov.constant.Constants.*;
//
///**
// * @ClassName com.tree.ncov.github
// * Description: <类功能描述>. <br>
// * <p>
// * <使用说明>
// * </p>
// * @Author tree
// * @Date 2020-02-14 13:40
// * @Version 1.0
// */
//public class InitGithubData {
//
//    /**
//     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
//     */
//    static Map<String, NcovCityDetail> addrDetailMap= new HashMap<>();
//    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
//    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//    static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
//
//
//    /**
//     * 相同年月日的不同省市做一个group
//     */
//    static Map<String/*年月日*/, Map<String/*省市*/, NcovCityDetail>/*省市*/> ncovMap = new HashMap<>();
//
//    public static void main(String[] args) throws Exception {
//        initJson();
////        initCsvData();
//    }
//
//    private static void initJson() throws Exception{
//        FileReader fileReader = new FileReader(new File(GITHUB_DATA_JSON_FILE_PATH));
//        BufferedReader br = new BufferedReader(fileReader);
//        String line = null;
//        StringBuilder sb = new StringBuilder(1024*50);
//        while ((line = br.readLine()) != null){
//            sb.append(line);
//        }
//        NcovCountryResult result = JSON.parseObject(sb.toString(), NcovCountryResult.class);
//        //处理数据, 获取有效的中国省市数据
//        List<NcovProvDetail> provDetails = result.getResults();
//        List<NcovProvDetail> chinaProvDetails = new ArrayList<>();
//        Map<String/*省*/,List<NcovCityDetail>> chinaProvCityMap = new HashMap<>();
//
//        provDetails.forEach(ncovProvDetail -> {
//            if(ncovProvDetail.getCountry().indexOf("中国") != -1){
//                chinaProvDetails.add(ncovProvDetail);
//                chinaProvCityMap.put(ncovProvDetail.getProvinceName(),ncovProvDetail.getCities());
//            }
//        });
//
//        //对比
//        //省数据有没有变化
//
//
//        //市数据有没有变化
//
//
//
//
//
//        System.out.println(chinaProvDetails.size());
//
//    }
//
//    private static void initCsvData() throws Exception{
//        readFile();
//        truncateTable();
//        initBatchUpdate(ncovMap);
//    }
//
//
//    private static void readFile() throws Exception{
//        FileReader fileReader = new FileReader(new File(GITHUB_DATA_CSV_FILE_PATH));
//        BufferedReader br = new BufferedReader(fileReader);
//
//        String line = null;
//        int i = 0;
//        NcovCityDetail detail = null;
//        String addr = null;
//        String city = null;
//        Map<String, NcovCityDetail> ncovDetailMap = new HashMap<>();
//        while ((line = br.readLine()) != null){
//            //第一行为表头， 忽略
//            if(i == 0 ){ i++; continue;}
//            String[] data = line.split(",");
//            detail = new NcovCityDetail();
//            addr = data[0];
//            city = data[1];
//            detail.setProvinceName(addr);
//            detail.setCityName(data[1]);
//            detail.setProvinceConfirmedCount(Long.valueOf(data[2]));
//            detail.setProvinceSuspectedCount(Long.valueOf(data[3]));
//            detail.setProvinceCuredCount(Long.valueOf(data[4]));
//            detail.setProvinceDeadCount(Long.valueOf(data[5]));
//
//            detail.setCityConfirmedCount(Long.valueOf(data[6]));
//            detail.setCitySuspectedCount(Long.valueOf(data[7]));
//            detail.setCityCuredCount(Long.valueOf(data[8]));
//            detail.setCityDeadCount(Long.valueOf(data[9]));
//            Date updateTime = null;
//            try {
//                updateTime = sdf.parse(data[10]);
//
//            }catch (Exception e){
//                try {
//                    updateTime = sdf2.parse(data[10]);
//                }catch (Exception e1){
//                    System.err.println(data[10]+"==="+JSON.toJSONString(detail));
//                }
//            }
//            detail.setUpdateTime(updateTime);
//
////            System.out.println(JSON.toJSONString(addrDetail));
//            String yearMonthDay = sdf3.format(updateTime);
//            String provCity = addr+city;
//
//            //如果年月日相同
//            if(ncovMap.containsKey(yearMonthDay)){
//                ncovDetailMap = ncovMap.get(yearMonthDay);
//                //如果省份城市相同
//                if(ncovDetailMap.containsKey(provCity) ){
//                    if(updateTime.getTime() > ncovDetailMap.get(provCity).getUpdateTime().getTime()){
//                        //更新
//                        ncovDetailMap.put(provCity, detail);
//                        ncovMap.put(yearMonthDay, ncovDetailMap);
//                    }else {
////                        System.out.println("重复条目， 忽略：" + JSON.toJSONString(detail));
//                    }
//                }else {
//                    ncovDetailMap.put(provCity, detail);
//                    ncovMap.put(yearMonthDay, ncovDetailMap);
//                }
//            }else {
//                ncovDetailMap = new HashMap<>();
//                ncovDetailMap.put(provCity,detail);
//                ncovMap.put(yearMonthDay, ncovDetailMap);
//
//            }
//
//            i++;
//        }
//
//
//    }
//
//    private static void truncateTable() {
//        DsUtil.execute(TRUNCATE_DETAIL_TABLE);
//    }
//
//    private static void initBatchUpdate(Map<String, Map<String, NcovCityDetail>> ncovMap) {
//        //插入次数，到99
//        int insertcount = 0;
//        //执行sql次数
//        int executeSqlNum = 0;
//        //所有数量
//        int allCount = 0;
//        //单层遍历次数
//        int travelCount1 = 0;
//        StringBuilder sql = new StringBuilder(1024*500);
//        sql.append(INSERT_NCOV_SQL_PREFIX);
//        StringBuilder valueSql = new StringBuilder(1024*500);
//        for(Map.Entry<String,Map<String, NcovCityDetail>> entry: ncovMap.entrySet()){
//
//            Map<String, NcovCityDetail> ncovDetailMap = entry.getValue();
//
//            allCount = allCount+ncovDetailMap.size();
//            System.out.println("当前日期:"+entry.getKey()+"，第【"+travelCount1+"】轮遍历，"+", 条数 = "+ncovDetailMap.size()+", 总条数 = "+(allCount));
//            for(Map.Entry<String, NcovCityDetail> detailEntry : ncovDetailMap.entrySet()){
//                NcovCityDetail detail = detailEntry.getValue();
//                valueSql.append("(")
//                        .append("'").append(detail.getProvinceName()).append("'").append(",")
//                        .append("'").append(detail.getCityName()).append("'").append(",")
//                        .append(detail.getProvinceConfirmedCount()).append(",")
//                        .append(detail.getProvinceSuspectedCount()).append(",")
//                        .append(detail.getProvinceCuredCount()).append(",")
//                        .append(detail.getProvinceDeadCount()).append(",")
//                        .append(detail.getCityConfirmedCount()).append(",")
//                        .append(detail.getCitySuspectedCount()).append(",")
//                        .append(detail.getCityCuredCount()).append(",")
//                        .append(detail.getCityDeadCount()).append(",")
//                        .append("'").append(sdf2.format(detail.getUpdateTime())).append("'")
//                        .append(")");
//
//                if(insertcount == 99) {
//                    valueSql.append(";");
//                    DsUtil.execute(sql.append(valueSql).toString());
//
//                    insertcount = 0;
//                    executeSqlNum++;
//                    //清空
//                    valueSql = new StringBuilder(1024*50);
//                    sql = new StringBuilder(1024*500);
//                    sql.append(INSERT_NCOV_SQL_PREFIX);
//                }else {
//                    valueSql.append(",");
//                    insertcount++;
//                }
//            }
//            travelCount1++;
//
//            if(travelCount1 == ncovMap.size() && valueSql.length() != 0){
//                String s = valueSql.substring(0,valueSql.length()-1);
//                DsUtil.execute(sql.append(s).toString());
//                executeSqlNum++;
//            }
//        }
//
//        System.out.println("遍历【"+travelCount1+"】次，执行sql【"+executeSqlNum+"】次，总共数量【"+allCount+"】");
//    }
//
//    private static void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
//        valueSql = new StringBuilder(1024*50);
//        sql = new StringBuilder(1024*500);
//        sql.append(INSERT_NCOV_SQL_PREFIX);
//    }
//}
