package com.tree.ncov.github.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;
import java.util.Optional;

/**
 * @ClassName com.tree.ncov.github.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-17 15:52
 * @Version 1.0
 */
@MappedSuperclass
@Data
public class NcovBaseEntity {
    @Column(name="country")
    private String countryName;
    @Column(name="current_confirm_count")
    private Long curConfirmCount;
    @Column(name="confirmed_count")
    private Long confirmedCount;
    @Column(name="suspected_count")
    private Long suspectedCount;
    @Column(name="cured_count")
    private Long curedCount;
    @Column(name="dead_count")
    private Long deadCount;
//    @Column(name="update_date")
//    private Date updateTime;
    @Column(name="createTime")
    private Date createTime;

    public Long getCurConfirmCount() {
        return Optional.ofNullable(curConfirmCount).orElse(0L);
    }

    public Long getConfirmedCount() {
        return Optional.ofNullable(confirmedCount).orElse(0L);
    }

    public Long getSuspectedCount() {
        return Optional.ofNullable(suspectedCount).orElse(0L);
    }

    public Long getCuredCount() {
        return Optional.ofNullable(curedCount).orElse(0L);
    }

    public Long getDeadCount() {
        return Optional.ofNullable(deadCount).orElse(0L);
    }

    public Date getCreateTime() {
        return Optional.ofNullable(createTime).orElse(new Date());
    }

}
