package com.tree.ncov.ncovdemo;

import com.alibaba.fastjson.JSON;
import com.tree.ncov.statdata.ProvinceDetailService;
import com.tree.ncov.statdata.dto.NcovOverallResult;
import com.tree.ncov.statdata.entity.NcovCityDetail;
import com.tree.ncov.statdata.entity.NcovCountry;
import com.tree.ncov.statdata.entity.NcovCountryLatest;
import com.tree.ncov.statdata.repository.CityDetailRepository;
import com.tree.ncov.statdata.repository.CountryLatestRepository;
import com.tree.ncov.statdata.repository.CountryRepository;
import com.tree.ncov.statdata.repository.ProvDetailRepository;
import com.tree.ncov.service.NcovDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest
class NcovDetailApplicationTests {
    @Autowired
    private NcovDetailService ncovService;
    @Value("${ncov.ds.name:mysql}")
    private String dsName;

    @Value("${ncov.githubdata.truncateable}")
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
    private CountryLatestRepository countryLatestRepository;
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
        ncovService.initDataFromLocal();
    }

    @Test
    public void updateAndCompare() throws Exception {
        ncovDetailService.compareAndUpdate();
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
    public void saveCountryLatest(){
        String s = "{\"confirmedCount\":74675,\"confirmedIncr\":399,\"countryName\":\"中国\",\"createTime\":1582166421712,\"curConfirmCount\":56385,\"curedCount\":16169,\"curedIncr\":1782,\"currentConfirmedCount\":56385,\"currentConfirmedIncr\":-1498,\"deadCount\":2121,\"deadIncr\":115,\"id\":204,\"seriousIncr\":-113,\"suspectedCount\":4922,\"suspectedIncr\":1277,\"updateTime\":1582165324307}\n";
        countryLatestRepository.save(JSON.parseObject(s, NcovCountryLatest.class));
    }

    @Test
    public void getOverAll(){
        NcovOverallResult o = restTemplate.getForObject("https://lab.isaaclin.cn/nCoV/api/overall", NcovOverallResult.class);
        System.out.println(JSON.toJSONString(o,true));

    }


}
