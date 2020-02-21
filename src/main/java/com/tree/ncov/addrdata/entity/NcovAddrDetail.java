package com.tree.ncov.addrdata.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

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
@Entity(name = "ncov_addr_detail")
@Data
public class NcovAddrDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 省
     */
    @Column(name="province")
    private String province;
    /**
     * 市
     */
    @Column(name="city")
    private String city;
    /**
     *
     */
    @Column(name="district")
    private String district;
    /**
     *
     */
    @Column(name="address")
    private String address;
    /**
     *
     */
    @Column(name="longitude")
    private String longitude;
    /**
     *
     */
    @Column(name="latitude")
    private String latitude;

    @Column(name="longitude_latitude")
    private String longitudeLatitude;
    /**
     *
     */
    @Column(name="count")
    private int count;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcovAddrDetail that = (NcovAddrDetail) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
