package com.tree.ncov.statdata.entity;

import lombok.Data;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @ClassName com.tree.ncov.statdata.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-15 12:28
 * @Version 1.0
 */
@Entity(name= "ncov_province_stat")
@Data
public class NcovProvDetail extends NcovBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="province")
    private String provinceName;
    @Column(name="province_short_name")
    private String provinceShortName;
    @Transient
    List<NcovCityDetail>  cities;
    @Column(name="update_date")
    private Date updateTime;

    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date(1580996305888L)));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        NcovProvDetail newProv = (NcovProvDetail)o;
        return this.provinceName.equals(newProv.provinceName);
    }
}
