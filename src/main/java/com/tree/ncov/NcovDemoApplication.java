package com.tree.ncov;

import static com.tree.ncov.constant.Constants.*;

import com.alibaba.fastjson.JSON;
import com.tree.ncov.cbndata.entity.NcovResult;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author tree
 */
@SpringBootApplication
public class NcovDemoApplication {

    @Autowired
    private static RedisTemplate redisTemplate;




    public static void main(String[] args) throws IOException {
        SpringApplication.run(NcovDemoApplication.class, args);

    }

    private static void readFile() {

    }

//    private static void downloadCsv() throws IOException{
//        RestTemplate restTemplate = new RestTemplate();
//        NcovResult o = restTemplate.getForObject(CBN_DATA_URL, NcovResult.class);
//
//        List<NcovAddrDetail> ncovAddrDetails = o.getData();
//        Iterator<NcovAddrDetail> it = ncovAddrDetails.iterator();
//        while (it.hasNext()){
//            NcovAddrDetail detail  = it.next();
//            if(StringUtils.isEmpty(detail.getAddress())
//                    ||StringUtils.isEmpty(detail.getLongitude())
//                    |StringUtils.isEmpty(detail.getLatitude())){
//                it.remove();
//            }
//        }
//
//        System.out.println(ncovAddrDetails.size());
//
//        FileUtils.writeStringToFile(new File(BASE_FOLDER,"肺炎具体地址经纬度.csv"), Json2Csv( JSON.toJSONString(ncovAddrDetails)));
//    }
//
//    public static String Json2Csv(String jsonstr) throws JSONException {
//        JSONArray jsonArray = new JSONArray(jsonstr);
//        //在内容开头加入UTF-8的BOM标识，如果用Excel打开没有这个会乱码的
//        String UTF_BOM_INFO = new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });
//        String csv =UTF_BOM_INFO+CDL.toString(jsonArray);
//        return csv;
//    }



}
