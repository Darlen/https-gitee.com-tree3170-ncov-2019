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
    String CBN_DATA_URL = "https://assets.cbndata.org/2019-nCoV/data.json";
    String CBN_DATA_CSV_FILE_NAME = "ncov_addr_detail.csv";
    String CBN_DATA_CSV_FILE_PATH = BASE_FOLDER+ CBN_DATA_CSV_FILE_NAME;
    String CBN_DATA_REDIS_KEY = "ncov:cbddata";
    String TRUNCATE_ADDR_TABLE = "truncate `ncov_addr_detail`";
    /**
     * 改分页查询
     */
    String SELECT_ALL_ADDR_TABLE = "select * from `ncov_addr_detail`";
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


    //==========================GITHUB

    String GITHUB_DATA_URL = "https://raw.githubusercontent.com/BlankerL/DXY-COVID-19-Data/master/json/DXYArea.json";
    String GITHUBU_DATA_REDIS_KEY = "ncov:github";
    String GITHUB_DATA_CSV_FILE_NAME = "DXYArea.csv";
    String GITHUB_DATA_CSV_FILE_PATH = BASE_FOLDER+ CBN_DATA_CSV_FILE_NAME;
    /**
     * 改分页查询
     */
    String TRUNCATE_DETAIL_TABLE = "truncate `ncov_detail`";
    String SELECT_ALL_DETAIL_TABLE = "select * from `ncov_detail`";


    /**
     * 插入ncov_detail前缀
     */
    String INSERT_NCOV_SQL_PREFIX="INSERT INTO ncov_detail (\n" +
            "\tprovinceName,\n" +
            "\tcityName,\n" +
            "\tprovince_confirmedCount,\n" +
            "\tprovince_suspectedCount,\n" +
            "\tprovince_curedCount,\n" +
            "\tprovince_deadCount,\n" +
            "\tcity_confirmedCount,\n" +
            "\tcity_suspectedCount,\n" +
            "\tcity_curedCount,\n" +
            "\tcity_deadCount,\n" +
            "\tupdateTime\n" +
            ")\n" +
            "VALUES";
}
