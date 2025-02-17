package com.tree.ncov.addrdata;

/**
 * @ClassName com.tree.ncov.addrdata
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 13:39
 * @Version 1.0
 */
public class CnbDataTest {
//    /**
//     * //TODO 持久化到redis
//     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
//     */
//    static Map<String, NcovAddrDetail> addrDetailMap= new HashMap<>();
//
//
//    public static void main(String[] args) throws IOException {
////        downloadJson2CsvToLocal();
//        readLocalCsv();
////        readFileFromRemote();
//        truncateTable();
//        initBatchUpdate(addrDetailMap);
//    }

//    private static void truncateTable() {
//        DsUtil.execute(TRUNCATE_ADDR_TABLE);
//    }
//
//    private static void readLocalCsv() throws IOException{
//
//        FileReader fileReader = new FileReader(new File(CBN_DATA_CSV_FILE_PATH));
//        BufferedReader br = new BufferedReader(fileReader);
//
//        String line = null;
//        int i = 0;
//        NcovAddrDetail addrDetail = null;
//        String addr = null;
//        String latitude = null;
//        String longtitude = null;
//
//        while ((line = br.readLine()) != null){
//            //第一行为表头， 忽略
//            if(i == 0 ){ i++; continue;}
//            String[] data = line.split(",");
//            addrDetail = new NcovAddrDetail();
//            //TODO 改为读JSON文件
//            addr = data[0];
//            latitude = data[4];
//            longtitude = data[6];
//            addrDetail.setAddress(addr);
//            addrDetail.setProvince(data[1]);
//            addrDetail.setCity(data[2]);
//            addrDetail.setDistrict(data[3]);
//            addrDetail.setLatitude(latitude);
//            int count = StringUtils.isEmpty(data[5])?0:Integer.valueOf(data[5]);
//            addrDetail.setCount(count);
//            addrDetail.setLongitude(longtitude);
//
//            if(StringUtils.isEmpty(addrDetail.getAddress())
//                    ||StringUtils.isEmpty(addrDetail.getLongitude())
//                    |StringUtils.isEmpty(addrDetail.getLatitude())){
//                System.out.println("无效数据， 忽略："+ JSON.toJSONString(addrDetail));
//                continue;
//            }
//
//            if(!addrDetailMap.containsKey(addr)) {
//                addrDetailMap.put(addr, addrDetail);
//            }else {
//                System.out.println("重复地址， 忽略："+ JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
//            }
//
//            i++;
//        }
//    }
//
//    private static void initBatchUpdate(Map<String, NcovAddrDetail> addrDetailMap) {
//        int insertCount = 0;
//        int executeSqlNum = 0;
//        StringBuilder sql = new StringBuilder(1024*50);
//        StringBuilder valueSql = new StringBuilder(1024*500);
//        NcovAddrDetail ncovAddrDetail = null;
//        sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
//        for(Map.Entry<String, NcovAddrDetail> entry: addrDetailMap.entrySet()){
//            ncovAddrDetail = entry.getValue();
//            valueSql.append("(")
//                    .append("'").append(ncovAddrDetail.getAddress()).append("'").append(",")
//                    .append("'").append(ncovAddrDetail.getProvince()).append("'").append(",")
//                    .append("'").append(ncovAddrDetail.getCity()).append("'").append(",")
//                    .append("'").append(ncovAddrDetail.getDistrict()).append("'").append(",")
//                    .append("'").append(ncovAddrDetail.getLatitude()).append("'").append(",")
//                    .append(ncovAddrDetail.getCount()).append(",")
//                    .append("'").append(ncovAddrDetail.getLongitude()).append("'")
//                    .append(")");
//
//            if(insertCount == 99) {
//                valueSql.append(";");
//                DsUtil.execute(sql.append(valueSql).toString());
//
//                insertCount = 0;
//                executeSqlNum++;
//
//                valueSql = new StringBuilder(1024*50);
//                sql = new StringBuilder(1024*500);
//                sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
//            }else {
//                valueSql.append(",");
//                insertCount++;
//            }
//        }
//
//        if(valueSql.length() != 0){
//            String s = valueSql.substring(0,valueSql.length()-1);
//            DsUtil.execute(sql.append(s).toString());
//            executeSqlNum++;
//
//        }
//
//        System.out.println("执行数据库【"+executeSqlNum+"】次，数据共【"+addrDetailMap.size()+"】条");
//    }
//
//    private static void emptyBufferSql(StringBuilder sql, StringBuilder valueSql) {
//        valueSql = new StringBuilder(1024*50);
//        sql = new StringBuilder(1024*500);
//        sql.append(INSERT_NCOV_SQL_ADDR_PREFIX);
//    }
//
//    private static List<NcovAddrDetail> readFileFromRemote() throws IOException{
//        RestTemplate restTemplate = new RestTemplate();
//        NcovResult o = restTemplate.getForObject(CBN_DATA_URL, NcovResult.class);
//
//        List<NcovAddrDetail> ncovAddrDetails = o.getData();
//        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
//        int duplicateCount = 0;
//        int allCount = ncovAddrDetails.size();
//        while (it.hasNext()){
//            NcovAddrDetail addrDetail  = it.next();
//            if(StringUtils.isEmpty(addrDetail.getAddress())
//                    ||StringUtils.isEmpty(addrDetail.getLongitude())
//                    |StringUtils.isEmpty(addrDetail.getLatitude())){
//                duplicateCount++;
//                System.out.println("无效数据， 忽略："+ JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
//                it.remove();
//                continue;
//            }
//            if(!addrDetailMap.containsKey(addrDetail.getAddress())) {
//                addrDetailMap.put(addrDetail.getAddress(), addrDetail);
//            }else {
//                duplicateCount++;
//                System.out.println("重复地址， 忽略："+ JSON.toJSONString(addrDetail, SerializerFeature.WriteNullStringAsEmpty));
//            }
//        }
//
//        System.out.println("总条数【"+allCount+"】， 重复条数【"+duplicateCount+"】， 实际条数【"+addrDetailMap.size()+"】");
//        return ncovAddrDetails;
//    }
//
//    private static void downloadJson2CsvToLocal() throws IOException{
//        List<NcovAddrDetail> ncovAddrDetails = readFileFromRemote();
//        FileUtils.writeStringToFile(new File(CBN_DATA_CSV_FILE_PATH),
//                Json2Csv(JSON.toJSONString(ncovAddrDetails)));
//    }
//
//    public static String Json2Csv(String jsonstr) throws JSONException {
//        JSONArray jsonArray = new JSONArray(jsonstr);
//        //在内容开头加入UTF-8的BOM标识，如果用Excel打开没有这个会乱码的
//        String UTF_BOM_INFO = new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });
//        String csv =UTF_BOM_INFO+ CDL.toString(jsonArray);
//        return csv;
//    }

}
