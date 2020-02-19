package com.tree.ncov.github.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @ClassName com.tree.ncov.github.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-15 12:46
 * @Version 1.0
 */
@Entity(name= "ncov_country_stat_latest")
@Data
public class NcovCountryLatest extends NcovBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Transient
    private List<NcovProvDetail> results;
    @Column(name="update_date")
    private Date updateTime;

    public NcovCountryLatest(NcovCountry country) {
        super.setCountryName(country.getCountryName());
        super.setCurConfirmCount(country.getCurConfirmCount());
        super.setConfirmedCount(country.getConfirmedCount());
        super.setSuspectedCount(country.getSuspectedCount());
        super.setCuredCount(country.getCuredCount());
        super.setDeadCount(country.getDeadCount());
        updateTime = country.getUpdateTime();

    }

}
