package com.tree.ncov.github.entity;

import lombok.Data;

import javax.persistence.*;
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
@Entity(name = "ncov_detail")
@Data
public class NcovCityDetail extends NcovBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Column(name = "country")
//    private String countryName;
    @Column(name = "province_name")
    private String provinceName;
    @Column(name = "province_short_name")
    private String provinceShortName;
    @Column(name = "city_name")
    private String cityName;
    @Column(name = "province_cur_confirmed_count")
    private Long provCurConfirmCount;
    @Column(name = "province_confirmed_count")
    private Long provinceConfirmedCount;
    @Column(name = "province_suspected_count")
    private Long provinceSuspectedCount;
    @Column(name = "province_dead_count")
    private Long provinceCuredCount;
    @Column(name = "province_cured_count")
    private Long provinceDeadCount;
//    @Column(name = "city_cur_confirmed_count")
//    private Long cityCurConfirmCount;
//    @Column(name = "city_confirmed_count")
//
//    private Long cityConfirmedCount;
//    @Column(name = "city_suspected_count")
//
//    private Long citySuspectedCount;
//    @Column(name = "city_cured_count")
//
//    private Long cityCuredCount;
//    @Column(name = "city_dead_count")
//
//    private Long cityDeadCount;

    @Column(name = "updateTime")
    private Date updateTime;

//    @Column(name = "createTime")
//    private Date createTime;

    public NcovCityDetail(){}

    public NcovCityDetail(String provinceName, String cityName){
        this.provinceName = provinceName;
        this.cityName = cityName;
    }

//    public Long getCityCurConfirmCount() {
//        return getCurConfirmCount() == null ? 0L: cityCurConfirmCount;
//    }
//
//    public Long getCityConfirmedCount() {
//        return cityConfirmedCount == null ? 0L: cityConfirmedCount;
//    }
//
//    public Long getCitySuspectedCount() {
//        return citySuspectedCount == null ? 0L: citySuspectedCount;
//    }
//
//    public Long getCityCuredCount() {
//        return cityCuredCount == null ? 0L: cityCuredCount;
//    }
//
//    public Long getCityDeadCount() {
//        return cityDeadCount == null ? 0L: cityDeadCount;
//    }
}
