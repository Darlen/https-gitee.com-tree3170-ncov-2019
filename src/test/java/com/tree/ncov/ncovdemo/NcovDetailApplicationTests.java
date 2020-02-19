package com.tree.ncov.ncovdemo;

import com.alibaba.fastjson.JSON;
import com.tree.ncov.cbndata.entity.NcovResult;
import com.tree.ncov.github.ProvinceDetailService;
import com.tree.ncov.github.dto.NcovOverallResult;
import com.tree.ncov.github.entity.NcovCityDetail;
import com.tree.ncov.github.entity.NcovCountry;
import com.tree.ncov.github.entity.NcovProvDetail;
import com.tree.ncov.github.repository.CityDetailRepository;
import com.tree.ncov.github.repository.CountryRepository;
import com.tree.ncov.github.repository.ProvDetailRepository;
import com.tree.ncov.service.NcovAddrService;
import com.tree.ncov.service.NcovDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootTest
class NcovDetailApplicationTests {
    @Autowired
    private NcovDetailService ncovService;
    @Value("${ncov.ds.name:mysql}")
    private String dsName;

    @Value("${ncov.githubdata.truncate}")
    boolean truncate;

    @Value("${ncov.githubdata.from}")
    private String from;

    @Value("${ncov.githubdata.remote.area.json.url}")
    private String remoteJsonUrl;

    @Value("${ncov.githubdata.remote.area.json.filename}")
    private String remoteJsonFilename;

    @Value("${ncov.githubdata.remote.area.zip.url}")
    private String remoteZipUrl;

    @Value("${ncov.githubdata.remote.area.zip.filename}")
    private String remoteZipFilename;

    @Value("${ncov.githubdata.local.json.url}")
    private String localJsonUrl;

    @Value("${ncov.githubdata.local.json.filename}")
    private String localJsonFilename;

    @Value("${ncov.githubdata.local.csv.url}")
    private String localCsvUrl;

    @Value("${ncov.githubdata.local.csv.filename}")
    private String localCsvFilename;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ProvinceDetailService provinceDetailService;

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private ProvDetailRepository provDetailRepository;

    @Autowired
    private CityDetailRepository cityDetailRepository;
    @Autowired
    private NcovDetailService ncovDetailService;
    @Autowired
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    /**
     * 初始化从local， remote只做update， 因为local是所有数据，remote是每天的数据
     * @throws Exception
     */
    @Test
    public void initData() throws Exception{
        //2020-02-17 12:46:48.504  INFO 12158 --- [           main] com.tree.ncov.service.NcovDetailService
        // : 执行sql【87】次，总共数量【8689】, 执行数据库总花费【2338】毫秒
        ncovService.initDataFromLocal();
    }

    @Test
    public void getValue(){
        System.out.println("dsName="+dsName);
        System.out.println("truncate="+truncate);
        System.out.println("from="+from);
        System.out.println("remoteJsonUrl="+remoteJsonUrl);
        System.out.println("remoteJsonFilename="+remoteJsonFilename);
        System.out.println("remoteZipUrl="+remoteZipUrl);
        System.out.println("remoteZipFilename="+remoteZipFilename);
        System.out.println("localJsonUrl="+localJsonUrl);
        System.out.println("localJsonFilename="+localJsonFilename);
        System.out.println("localCsvFilename="+localCsvFilename);
        System.out.println("localCsvUrl="+localCsvUrl);
    }

    @Test
    public void updateAndCompare() throws Exception {
        ncovDetailService.compareAndUpdate();
    }

    @Test
    public void queryForCountry(){
        List<NcovCountry> countryList = jdbcTemplate.queryForList("select country from ncov_country_stat", NcovCountry.class);
        System.out.println(JSON.toJSONString(countryList));
    }

    @Test
    public void updateProvinceTodayData(){
        provinceDetailService.updateProvinceTodayData();
    }

    @Test
    public void getTodayCountryDetailFromDB(){
        NcovCountry ncovCountry = provinceDetailService.getTodayCountryDetailFromDB();
        System.out.println();
        System.out.println(JSON.toJSONString(ncovCountry));
    }


    @Test
    public void findLatestByProvince(){
        NcovCityDetail cityDetail =  cityDetailRepository.findLatestByProvince("广东省");
        System.out.println(JSON.toJSONString(cityDetail));

    }

    @Test
    public void getOverAll(){
        NcovOverallResult o = restTemplate.getForObject("https://lab.isaaclin.cn/nCoV/api/overall", NcovOverallResult.class);
        System.out.println(JSON.toJSONString(o,true));

    }


}
