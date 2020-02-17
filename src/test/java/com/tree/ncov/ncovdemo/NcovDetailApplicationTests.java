package com.tree.ncov.ncovdemo;

import com.tree.ncov.service.NcovAddrService;
import com.tree.ncov.service.NcovDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Value("${ncov.githubdata.remote.json.url}")
    private String remoteJsonUrl;

    @Value("${ncov.githubdata.remote.json.filename}")
    private String remoteJsonFilename;

    @Value("${ncov.githubdata.remote.zip.url}")
    private String remoteZipUrl;

    @Value("${ncov.githubdata.remote.zip.filename}")
    private String remoteZipFilename;

    @Value("${ncov.githubdata.local.json.url}")
    private String localJsonUrl;

    @Value("${ncov.githubdata.local.json.filename}")
    private String localJsonFilename;

    @Value("${ncov.githubdata.local.csv.url}")
    private String localCsvUrl;

    @Value("${ncov.githubdata.local.csv.filename}")
    private String localCsvFilename;

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
    public void updateData(){
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

}
