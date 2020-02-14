package com.tree.ncov.constant;

/**
 * @ClassName com.tree.ncov.constant
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 14:21
 * @Version 1.0
 */
public interface Constants {
    String BASE_FOLDER = "/Users/tree/Desktop/ncov/";
    String CBN_DATA_URL = "https://assets.cbndata.org/2019-nCoV/data.json?t=1581305742720";


    String INSERT_NCOV_SQL_ADDR_PREFIX = "INSERT INTO ncov_addr_detail (\n"+
            "\taddress,\n"+
            "\tprovince,\n"+
            "\tcity,\n"+
            "\tdistrict,\n"+
            "\tlatitude,\n"+
            "\tcount,\n"+
            "\tlongitude\n"+
            ")\n"+
            "VALUES";
    /**
     * 插入ncov_detail前缀
     */
    String INSERT_NCOV_SQL_PREFIX="insert into ncov_detail(provinceName,cityName" +
            ",province_confirmedCount,province_suspectedCount,province_curedCount,province_deadCount" +
            ",city_confirmedCount,city_suspectedCount,city_curedCount,city_deadCount" +
            ",updateTime) values";
}
