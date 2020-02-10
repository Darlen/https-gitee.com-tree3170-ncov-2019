package com.tree.ncov.entity;

import java.util.List;

/**
 * @ClassName com.tree.ncov.entity
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-10 14:54
 * @Version 1.0
 */
@lombok.Data
public class Ncov {
    private List<NcovAddrDetail> data;
}
