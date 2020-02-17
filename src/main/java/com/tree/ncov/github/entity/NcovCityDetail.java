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
    private Long provCurConfirmCount;
    private Long provinceConfirmedCount;
    private Long provinceSuspectedCount;
    private Long provinceCuredCount;
    private Long provinceDeadCount;
    private Long cityCurConfirmCount;
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

    public Long getCityCurConfirmCount() {
        return cityCurConfirmCount == null ? 0L: cityCurConfirmCount;
    }

    public Long getCityConfirmedCount() {
        return cityConfirmedCount == null ? 0L: cityConfirmedCount;
    }

    public Long getCitySuspectedCount() {
        return citySuspectedCount == null ? 0L: citySuspectedCount;
    }

    public Long getCityCuredCount() {
        return cityCuredCount == null ? 0L: cityCuredCount;
    }

    public Long getCityDeadCount() {
        return cityDeadCount == null ? 0L: cityDeadCount;
    }
}
