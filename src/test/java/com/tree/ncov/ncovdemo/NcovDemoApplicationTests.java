package com.tree.ncov.ncovdemo;

import com.tree.ncov.service.NcovAddrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NcovDemoApplicationTests {
    @Autowired
    private NcovAddrService ncovService;

    @Test
    void contextLoads() {
    }

    @Test
    public void setValue(){
    }

}
