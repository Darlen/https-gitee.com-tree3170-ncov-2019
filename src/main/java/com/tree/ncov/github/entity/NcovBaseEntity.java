package com.tree.ncov.github.entity;

import lombok.Data;

import java.util.Date;

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
@Data
public class NcovBaseEntity {
    private Long curConfirmCount;
    private Long confirmedCount;
    private Long suspectedCount;
    private Long curedCount;
    private Long deadCount;
    private Date updateTime;

    public Long getCurConfirmCount() {
        return curConfirmCount==null?0L:curConfirmCount;
    }

    public Long getConfirmedCount() {
        return confirmedCount==null?0L:confirmedCount;
    }

    public Long getSuspectedCount() {
        return suspectedCount==null?0L:suspectedCount;
    }

    public Long getCuredCount() {
        return curedCount==null?0L:curedCount;
    }

    public Long getDeadCount() {
        return deadCount==null?0L:deadCount;
    }
}
