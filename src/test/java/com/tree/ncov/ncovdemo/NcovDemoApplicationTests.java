package com.tree.ncov.ncovdemo;

import com.tree.ncov.service.NcovService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NcovDemoApplicationTests {
    @Autowired
    private NcovService ncovService;

    @Test
    void contextLoads() {
    }

    @Test
    public void setValue(){
    }

}
