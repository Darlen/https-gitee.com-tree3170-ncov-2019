package com.tree.ncov.service;

import java.util.Map;

/**
 * @ClassName com.tree.ncov.service
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-15 12:11
 * @Version 1.0
 */
public abstract class AbstractService {

    /**
     * 下载文件
     */
    abstract void downloadFile();

    /**
     * 读取本地文件
     */
    abstract void readLocalFile();

    /**
     * 初始化表， 如truncate table
     */
    abstract void initTable();

    /**
     * 批量插入数据到数据库
     * @param map
     */
    abstract void batchInsert(Map<String, Object> map);

}
