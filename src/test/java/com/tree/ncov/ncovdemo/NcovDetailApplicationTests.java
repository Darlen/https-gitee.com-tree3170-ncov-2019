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

    @Test
    public void initData() throws Exception{
        ncovService.initDataFromLocal();
    }

    @Test
    public void updateData(){
        System.out.println(dsName);
        System.out.println(from);
        System.out.println(remoteJsonUrl);
        System.out.println(remoteJsonFilename);
        System.out.println(remoteZipUrl);
        System.out.println(remoteZipFilename);
        System.out.println(localJsonUrl);
        System.out.println(localJsonFilename);
        System.out.println(localCsvFilename);
        System.out.println(localCsvUrl);
    }

}
