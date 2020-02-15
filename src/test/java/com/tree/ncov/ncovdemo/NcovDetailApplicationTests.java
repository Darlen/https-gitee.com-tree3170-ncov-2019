package com.tree.ncov.ncovdemo;

import com.tree.ncov.service.NcovAddrService;
import com.tree.ncov.service.NcovDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NcovDetailApplicationTests {
    @Autowired
    private NcovDetailService ncovService;

    @Test
    void contextLoads() {
    }

    @Test
    public void initData() throws Exception{
        ncovService.initJson();
        ncovService.initCsvData();
    }

}
