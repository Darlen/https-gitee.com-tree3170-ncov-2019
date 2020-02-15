package com.tree.ncov.github.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName com.tree.ncov.github.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 13:36
 * @Version 1.0
 */
@Data
public class NcovCityDetail {
    private String country = "中国";
    private String provinceName;
    private String provinceShortName;
    private String cityName;
    private Long provinceConfirmedCount;
    private Long provinceSuspectedCount;
    private Long provinceCuredCount;
    private Long provinceDeadCount;
    private Long cityConfirmedCount;
    private Long citySuspectedCount;
    private Long cityCuredCount;
    private Long cityDeadCount;
    private Date updateTime;
    private Date createTime;

    public NcovCityDetail(){}

    public NcovCityDetail(String provinceName, String cityName){
        this.provinceName = provinceName;
        this.cityName = cityName;
    }


//    @Override
//    public int hashCode() {
//        return super.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        NcovCityDetail detail = (NcovCityDetail) obj;
//        String provCity = detail.getProvinceName()+detail.getCityName();
//        return (this.provinceName+this.cityName).equals(provCity);
//    }

    public static void main(String[] args) {
        List<NcovCityDetail> details = new ArrayList<>();
        details.add(new NcovCityDetail("t1","c1"));
        details.add(new NcovCityDetail("t2","c2"));
        details.add(new NcovCityDetail("t3","c1"));

        System.out.println(details.contains(new NcovCityDetail("t1","c1")));
    }
}
