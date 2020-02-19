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
@Entity(name= "ncov_country_stat")
@Data
public class NcovCountry extends NcovBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Transient
    private List<NcovProvDetail> results;
    @Column(name="update_date")
    private Date updateTime;


    /**
     *
     * 现存确诊人数（较昨日增加数量）
     * 值为confirmedCount(Incr) - curedCount(Incr) - deadCount(Incr)
     */
    @Column(name="current_confirmed_Incr")
    private Long currentConfirmedIncr;

    /**
     * 累计确诊人数（较昨日增加数量）
     */
    @Column(name="confirmed_incr")
    private Long confirmedIncr;
    /**
     * 疑似感染人数（较昨日增加数量）
     */
    @Column(name="suspected_incr")
    private Long suspectedIncr;
    /**
     * 治愈人数（较昨日增加数量）
     */
    @Column(name="cured_incr")
    private Long curedIncr;
    /**
     * 死亡人数（较昨日增加数量）
     */
    @Column(name="dead_incr")
    private Long deadIncr;
    /**
     * 重症病例人数（较昨日增加数量）
     */
    @Column(name="serious_incr")
    private Long seriousIncr;
}
