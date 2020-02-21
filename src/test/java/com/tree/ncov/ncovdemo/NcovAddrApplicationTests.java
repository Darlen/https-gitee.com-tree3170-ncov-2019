package com.tree.ncov.ncovdemo;

import com.tree.ncov.addrdata.entity.NcovAddrDetail;
import com.tree.ncov.addrdata.repository.AddrRepository;
import com.tree.ncov.service.NcovAddrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NcovAddrApplicationTests {
    @Autowired
    private NcovAddrService addrService;
    @Autowired
    private AddrRepository addrRepository;

    @Test
    void contextLoads() {
    }

    @Test
    public void localInitData() throws Exception{
        addrService.initDataFromLocal();
    }

    @Test
    public void remoteInitData() throws Exception{
        addrService.initDataFromRemote();
    }

    @Test
    public void getAllData() throws Exception{
       List<NcovAddrDetail> addrDetailList =  addrRepository.findAll();
        System.out.println(1);
    }

    @Test
    public void compareAndUpdate() throws Exception{
        addrService.compareAndUpdate();
        System.out.println(1);
    }


}
