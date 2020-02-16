package com.tree.ncov.ncovdemo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.redis.impl.RedisService;
import com.tree.ncov.service.NcovAddrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static com.tree.ncov.constant.Constants.CBN_DATA_REDIS_KEY;

@SpringBootTest
class NcovAddrApplicationTests {
    @Autowired
    private NcovAddrService ncovService;
    @Autowired
    private  RedisService redisService;

    @Test
    void contextLoads() {
    }

    @Test
    public void redis(){
//        JSONObject map = (JSONObject)redisService.get(CBN_DATA_REDIS_KEY);
//        System.out.println(1);
//                JSONObject.parseObject(
//                map,
//                new TypeReference<T>(){});
    }
    @Test
    public void initData() throws Exception{

//        ncovService.initDataFromLocal();
        ncovService.initDataFromRemote();
    }

}
