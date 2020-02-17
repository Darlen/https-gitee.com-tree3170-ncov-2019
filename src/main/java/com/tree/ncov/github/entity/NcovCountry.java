package com.tree.ncov.github.entity;

import lombok.Data;

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
@Data
public class NcovCountry extends NcovBaseEntity{
    private List<NcovProvDetail> results;
    private String country;
}
