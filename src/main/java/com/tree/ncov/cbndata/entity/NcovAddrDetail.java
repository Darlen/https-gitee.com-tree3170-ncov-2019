package com.tree.ncov.cbndata.entity;

import lombok.Data;

/**
 * @ClassName com.tree.ncov.entity
 * Description: 肺炎感染
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-10 14:20
 * @Version 1.0
 */
@Data
public class NcovAddrDetail {
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     *
     */
    private String district;
    /**
     *
     */
    private String address;
    /**
     *
     */
    private String longitude;
    /**
     *
     */
    private String latitude;

    private String longitudeLatitude;
    /**
     *
     */
    private int count;
}
