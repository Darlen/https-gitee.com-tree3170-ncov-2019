package com.tree.ncov.github.entity;

import lombok.Data;

import javax.persistence.*;
import java.text.SimpleDateFormat;
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
 * @Date 2020-02-15 12:28
 * @Version 1.0
 */
@Entity(name= "ncov_province_stat_latest")
@Data
public class NcovProvDetailLatest extends NcovBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="province")
    private String provinceName;
    @Column(name = "province_short_name")
    private String provinceShortName;
    @Transient
    List<NcovCityDetail> cities;
    @Column(name="update_date")
    private Date updateTime;

    public NcovProvDetailLatest(NcovProvDetail provDetail) {
        this.setCountryName(provDetail.getCountryName());
        this.provinceName = provDetail.getProvinceName();
        provinceShortName = provDetail.getProvinceShortName();
        super.setCurrentConfirmedCount(provDetail.getCurConfirmCount());
        super.setConfirmedCount(provDetail.getConfirmedCount());
        super.setSuspectedCount(provDetail.getSuspectedCount());
        super.setCuredCount(provDetail.getCuredCount());
        super.setDeadCount(provDetail.getDeadCount());
        updateTime = provDetail.getUpdateTime();
    }

    public static List<NcovProvDetailLatest> convert(List<NcovProvDetail> ncovProvDetails){
        List<NcovProvDetailLatest> ncovProvDetailLatests = new ArrayList<>();
        ncovProvDetails.forEach(provDetail -> {
            ncovProvDetailLatests.add(new NcovProvDetailLatest(provDetail));
        });
        return ncovProvDetailLatests;
    }

    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date(1580996305888L)));
    }

}
