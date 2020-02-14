package com.tree.ncov.cbndata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.cbndata.entity.NcovResult;
import com.tree.ncov.github.entity.NcovDetail;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tree.ncov.constant.Constants.*;

/**
 * @ClassName com.tree.ncov.cbndata
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 13:39
 * @Version 1.0
 */
public class CnbDataTest {
    /**
     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
     */
    static Map<String, NcovAddrDetail> addrDetailMap= new HashMap<>();

    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader(new File(BASE_FOLDER+"肺炎具体地址经纬度.csv"));

        BufferedReader br = new BufferedReader(fileReader);

        String line = null;
        int i = 0;
        NcovAddrDetail addrDetail = null;
        String addr = null;
        String latitude = null;
        String longtitude = null;

        while ((line = br.readLine()) != null){
            if(i == 0 ){ i++; continue;}
            String[] data = line.split(",");
            addrDetail = new NcovAddrDetail();
            addr = data[0];
            latitude = data[4];
            longtitude = data[6];
            addrDetail.setAddress(addr);
            addrDetail.setProvince(data[1]);
            addrDetail.setCity(data[2]);
            addrDetail.setDistrict(data[3]);
            addrDetail.setLatitude(latitude);
            int count = StringUtils.isEmpty(data[5])?0:Integer.valueOf(data[5]);
            addrDetail.setCount(count);
            addrDetail.setLongitude(longtitude);

            if(!addrDetailMap.containsKey(addr)) {
                addrDetailMap.put(addr, addrDetail);
            }else {
                System.out.println("重复地址， 忽略："+ JSON.toJSONString(addrDetail));
            }

            i++;
        }

        int count = 0;
        int num = 0;


        StringBuilder sql = new StringBuilder(1024*50);
        NcovAddrDetail ncovAddrDetail = null;
        sql.append("insert into ncov_addr_detail(address,province,city,district,latitude,count,longitude) values");
        for(Map.Entry<String, NcovAddrDetail> entry: addrDetailMap.entrySet()){
            ncovAddrDetail = entry.getValue();
            sql.append("(")
                    .append("'").append(ncovAddrDetail.getAddress()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getProvince()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getCity()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getDistrict()).append("'").append(",")
                    .append("'").append(ncovAddrDetail.getLatitude()).append("'").append(",")
                    .append(ncovAddrDetail.getCount()).append(",")
                    .append("'").append(ncovAddrDetail.getLongitude()).append("'")
                    .append(")");

            if(count == 100) {
                num++;
                sql.append(";");
                count = 0;
                DsUtil.execute(sql.toString());
                sql = new StringBuilder(1024*50);
                sql.append("insert into ncov_addr_detail(address,province,city,district,latitude,count,longitude) values");


            }else {
                sql.append(",");
                count++;
            }


        }

        System.out.println("size = "+addrDetailMap.size()+", num = "+num);

    }


}
