package com.tree.ncov.service;

import java.io.IOException;
import java.util.List;

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

    public  void initDataFromLocal() throws IOException{
        initTable();
        batchUpdate(readFileFromLocal());
    }

    public  void initDataFromRemote() throws IOException{
        initTable();
        batchUpdate(readFileFromRemote());
    }

    public void compareAndUpdate() throws Exception {

    }

    /**
     * 下载文件
     */
    public  void downloadFile2Local() throws IOException{

    }

    /**
     * 读取本地文件
     */
    public abstract List readFileFromLocal() throws IOException;

    /**
     * 从远程读取数据
     */
    public abstract List readFileFromRemote() throws IOException;

    /**
     *
     * @param ncovList
     */
    public abstract void putDataInRedis(List ncovList);

    /**
     * 初始化表， 如truncate table
     */
    public abstract void initTable();

    /**
     * 批量插入数据到数据库
     */
    public abstract void batchUpdate(List ncovList);




}
