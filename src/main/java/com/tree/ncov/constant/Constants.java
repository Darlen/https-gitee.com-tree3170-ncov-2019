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
    String CHINA = "中国";
    String BASE_FOLDER = "/Users/tree/Desktop/ncov/";
    String CBN_DATA_URL = "https://assets.cbndata.org/2019-nCoV/data.json";
    String CBN_DATA_CSV_FILE_NAME = "ncov_addr_detail.csv";
    String CBN_DATA_CSV_FILE_PATH = BASE_FOLDER+ CBN_DATA_CSV_FILE_NAME;
    String CBN_DATA_REDIS_KEY = "ncov:cbddata";
    String TRUNCATE_ADDR_TABLE = "truncate ncov_addr_detail";
    String TRUNCATE_COUNTRY_TABLE = "truncate ncov_country_stat_latest";
    String TRUNCATE_COUNTRY_LATEST_TABLE = "truncate ncov_country_stat";
    String TRUNCATE_PROVINCE_TABLE = "truncate ncov_province_stat_latest";
    String TRUNCATE_PROVINCE_LATEST_TABLE = "truncate ncov_province_stat";
    /**
     * 改分页查询
     */
    String SELECT_ALL_ADDR_TABLE = "select * from ncov_addr_detail";
    String INSERT_NCOV_SQL_ADDR_PREFIX = "INSERT INTO ncov_addr_detail (\n"+
            "\taddress,\n"+
            "\tprovince,\n"+
            "\tcity,\n"+
            "\tdistrict,\n"+
            "\tlatitude,\n"+
            "\tcount,\n"+
            "\tlongitude,\n"+
            "\tlongitude_latitude\n"+
            ")\n"+
            "VALUES";


    //==========================GITHUB

    String GITHUB_DATA_URL = "https://raw.githubusercontent.com/BlankerL/DXY-COVID-19-Data/master/json/DXYArea.json";
    /**
     * 具体每一天
     */
    String GITHUBU_DATA_DAYS = "ncov:statdata:days";

    /**
     * 每天国家统计数量集合
     */
    String GITHUBU_DATA_COUNTRY_BY_DAY_REDIS_KEY = "ncov:statdata:day:country:";
    /**
     * 当天国家统计数据
     */
    String GITHUBU_DATA_COUNTRY_CURRENT_BY_DAY_REDIS_KEY = "ncov:statdata:day:country:current";

    /**
     * 每天省份统计数量集合
     */
    String GITHUBU_DATA_PPROVINCE_BY_DAY_REDIS_KEY = "ncov:statdata:day:province:";

    /**
     * 当天省份统计数据
     */
    String GITHUBU_DATA_PPROVINCE_CURRENT_BY_DAY_REDIS_KEY = "ncov:statdata:day:province:current";

    /**
     * 每天省对应城市的新增统计数量集合
     */
    String GITHUBU_DATA_PROV_CITY_BY_DAY_REDIS_KEY = "ncov:statdata:day:city:";
    String GITHUB_DATA_CSV_FILE_NAME = "DXYArea.csv";
    String GITHUB_DATA_JSON_FILE_NAME = "DXYArea.json";
    String GITHUB_DATA_CSV_FILE_PATH = BASE_FOLDER+ GITHUB_DATA_CSV_FILE_NAME;
    String GITHUB_DATA_JSON_FILE_PATH = BASE_FOLDER+ GITHUB_DATA_JSON_FILE_NAME;
    /**
     * 改分页查询
     */
    String TRUNCATE_DETAIL_TABLE = "truncate ncov_detail";
    String SELECT_ALL_DETAIL_TABLE = "select * from `ncov_detail`";


    /**
     * 插入ncov_detail前缀
     */
    String INSERT_NCOV_SQL_PREFIX="INSERT INTO ncov_detail (\n" +
            "\tcountry,\n" +
            "\tprovince_name,\n" +
            "\tprovince_short_name,\n" +
            "\tcity_name,\n" +
            "\tprovince_cur_confirmed_count,\n" +
            "\tprovince_confirmed_count,\n" +
            "\tprovince_suspected_count,\n" +
            "\tprovince_cured_count,\n" +
            "\tprovince_dead_count,\n" +
            "\tcurrent_confirm_count,\n" +
            "\tconfirmed_count,\n" +
            "\tsuspected_count,\n" +
            "\tcured_count,\n" +
            "\tdead_count,\n" +
            "\tupdateTime\n" +
            ")\n" +
            "VALUES";
    /** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
    String NORM_DATETIME_MS_PATTERN = ("yyyy-MM-dd HH:mm:ss.SSS");
    /** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
    String NORM_DATETIME_MS_PATTERN2 = ("yyyy/MM/dd HH:mm:ss.SSS");
    /** 标准日期格式：yyyyMMddHHmmss */
    String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";
    /** 标准日期格式：yyyy-MM-dd */
    String NORM_DATE_PATTERN = "yyyy-MM-dd";
    /** 标准日期格式：yyyyMMdd */
    String PURE_DATE_PATTERN = "yyyyMMdd";

}
