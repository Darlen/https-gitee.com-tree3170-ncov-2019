package com.tree.ncov.github.entity;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @ClassName com.tree.ncov.github.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-15 12:28
 * @Version 1.0
 */
@Data
public class NcovProvDetail extends NcovBaseEntity{
    private String country = "中国";
    private String provinceName;
    private String provinceShortName;
//    private Long curConfirmCount;
//    private Long confirmedCount;
//    private Long suspectedCount;
//    private Long curedCount;
//    private Long deadCount;
//    private Date updateTime;
    List<NcovCityDetail>  cities;

    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date(1580996305888L)));
    }

}
