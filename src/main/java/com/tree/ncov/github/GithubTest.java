package com.tree.ncov.github;

import com.alibaba.fastjson.JSON;
import com.tree.ncov.Util.DsUtil;
import com.tree.ncov.cbndata.entity.NcovAddrDetail;
import com.tree.ncov.github.entity.NcovDetail;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tree.ncov.constant.Constants.BASE_FOLDER;

/**
 * @ClassName com.tree.ncov.github
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 13:40
 * @Version 1.0
 */
public class GithubTest {

    /**
     * key 为Address+Latitude+Longitude, 作为缓存， 去除重复的key
     */
    static Map<String, NcovDetail> addrDetailMap= new HashMap<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

//    static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
    /**
     *
     */
    static Map<String/*年月日*/, Map<String/*省市*/, NcovDetail>> ncovMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        FileReader fileReader = new FileReader(new File(BASE_FOLDER+"DXYArea.csv"));

        BufferedReader br = new BufferedReader(fileReader);

        String line = null;
        int i = 0;
        NcovDetail addrDetail = null;
        String addr = null;
        String city = null;

        while ((line = br.readLine()) != null){
            if(i == 0 ){ i++; continue;}
            String[] data = line.split(",");
            addrDetail = new NcovDetail();
            addr = data[0];
            city = data[1];
            addrDetail.setProvinceName(addr);
            addrDetail.setCityName(data[1]);
            addrDetail.setProvinceConfirmedCount(data[2]);
            addrDetail.setProvinceSuspectedCount(Long.valueOf(data[3]));
            addrDetail.setProvinceCuredCount(Long.valueOf(data[4]));
            addrDetail.setProvinceDeadCount(Long.valueOf(data[5]));

            addrDetail.setCityConfirmedCount(Long.valueOf(data[6]));
            addrDetail.setCitySuspectedCount(Long.valueOf(data[7]));
            addrDetail.setCityCuredCount(Long.valueOf(data[8]));
            addrDetail.setCityDeadCount(Long.valueOf(data[9]));
            Date d = null;
            try {
                d = sdf.parse(data[10]);

            }catch (Exception e){
                System.err.println(data[10]+"==="+JSON.toJSONString(addrDetail));
                d = sdf2.parse(data[10]);
            }
            addrDetail.setUpdateTime(d);
            addrDetail.setCreateTime(new Date());

//            System.out.println(JSON.toJSONString(addrDetail));
            String key =  d.getYear()+d.getMonth()+d.getDay()+"";
            String key1 = addr+city;

            if(ncovMap.containsKey(key)){
                Map<String,NcovDetail> ncovDetailMap = ncovMap.get(key);
                if(ncovDetailMap.containsKey(key1) ){
                    if(d.getTime() > ncovDetailMap.get(key1).getUpdateTime().getTime()){
                        //更新
                    }else {
                        System.out.println("重复地址， 忽略：" + JSON.toJSONString(addrDetail));
                    }
                }else {
                    ncovDetailMap.put(key1, addrDetail);
                    ncovMap.put(key, ncovDetailMap);
                }
            }

            addrDetailMap.put(key,addrDetail);
            if(!addrDetailMap.containsKey(key)) {
                addrDetailMap.put(addr, addrDetail);
            }else {
                System.out.println("重复地址， 忽略："+ JSON.toJSONString(addrDetail));
            }
            i++;
        }

        int count = 0;
        int num = 0;



        System.out.println("size = "+addrDetailMap.size()+", num = "+num);

    }
}
