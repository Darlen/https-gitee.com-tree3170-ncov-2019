package com.tree.ncov.ncovdemo;

import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.service.NcovAddrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NcovAddrApplicationTests {
    @Autowired
    private NcovAddrService ncovService;

    @Test
    void contextLoads() {
    }

    @Test
    public void initData() throws Exception{
        //        downloadJson2CsvToLocal();
        ncovService.readLocalCsv();
        List<NcovAddrDetail> ncovAddrDetails = ncovService.readRemoteJsonFile();
        ncovService.putDataInRedis(ncovAddrDetails);
        ncovService.truncateTable();
        ncovService.batchInsert();
    }

}
