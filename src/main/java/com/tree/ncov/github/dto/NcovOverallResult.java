package com.tree.ncov.github.dto;

import com.tree.ncov.github.entity.NcovCountry;
import com.tree.ncov.github.entity.NcovCountryLatest;

import java.util.List;

/**
 * @ClassName com.tree.ncov.github.dto
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-19 19:28
 * @Version 1.0
 */
@lombok.Data
public class NcovOverallResult {
    private List<NcovCountry> results;
    boolean success;
}
