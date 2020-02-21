package com.tree.ncov.statdata.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @ClassName com.tree.ncov.statdata.entity
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
    @Column(name = "province_cured_count")
    private Long provinceCuredCount;
    @Column(name = "province_dead_count")
    private Long provinceDeadCount;

    @Column(name = "updateTime")
    private Date updateTime;


    public NcovCityDetail(){}

    public NcovCityDetail(String provinceName, String cityName){
        this.provinceName = provinceName;
        this.cityName = cityName;
    }

}
