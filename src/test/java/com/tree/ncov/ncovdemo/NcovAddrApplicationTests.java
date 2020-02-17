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
    public void localInitData() throws Exception{
        //2020-02-17 10:12:15.178  INFO 95243 --- [           main] com.tree.ncov.service.NcovAddrService              :
        // ==>执行[putDataInRedis], 总条数【6294】, 重复条数【3】，去除重复数据之后实际条数【6291】, 共花费【802】毫秒
        ncovService.initDataFromLocal();
    }

    @Test
    public void remoteInitData() throws Exception{
        ncovService.initDataFromRemote();
    }

    @Test
    public void compareAndUpdate() throws Exception{

        ncovService.compareAndUpdate();
    }
}
