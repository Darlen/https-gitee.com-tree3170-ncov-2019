package com.tree.ncov.github.entity;

import lombok.Data;

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
@Data
public class NcovProvDetail {
    private String country = "中国";
    private String provinceName;
    private String provinceShortName;
    private Long confirmedCount;
    private Long suspectedCount;
    private Long curedCount;
    private Long deadCount;
    List<NcovCityDetail>  cities;

}
