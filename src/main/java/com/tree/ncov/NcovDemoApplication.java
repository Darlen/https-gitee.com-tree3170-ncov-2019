package com.tree.ncov;

import static com.tree.ncov.constant.Constants.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tree.ncov.cbndata.entity.NcovResult;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.redis.impl.RedisService;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author tree
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class NcovDemoApplication {


    public static void main(String[] args) throws IOException {
        SpringApplication.run(NcovDemoApplication.class, args);


    }





}
