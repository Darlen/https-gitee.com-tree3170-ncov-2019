package com.tree.ncov.service;

import cn.hutool.core.date.TimeInterval;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName com.tree.ncov.service
 * Description: 抽象服务类
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-15 12:11
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractService {

    /**
     * 两种初始化方式：
     *      第一种为本地初始化，从本地加载文件初始化数据到数据库
     *
     * 1. 初始化表
     * 2. 从本地加载集合对象, 并简单处理
     * 3. 继续处理该集合对象， 并放入redis
     * 4. 放入数据库
     *
     * @throws IOException
     */
    public  void initDataFromLocal() throws IOException{
        TimeInterval timeInterval = new TimeInterval();
        log.info("################开始执行[{}]类的方法[initDataFromLocal]###################",this.getClass().getSimpleName());
        log.info("执行初始化表数据...");
        initTable();

        log.info("执行读取远程数据...");
        List list = readFileFromLocal();
        putDataInRedis(list);
        log.info("执行初始化批量更新操作...");
        initBatchUpdate(list);
        log.info("################结束执行[{}]类的方法[initDataFromLocal], 共花费【{}】毫秒###################",
                this.getClass().getSimpleName(),timeInterval.interval());

    }

    /**
     * 两种初始化方式：
     *      第二种为远程初始化，从远程加载文件并初始化到数据库
     *
     * 1. 初始化表
     * 2. 从本地加载集合对象, 并简单处理
     * 3. 继续处理该集合对象， 并放入redis
     * 4. 放入数据库
     *
     * @throws Exception
     */
    public  void initDataFromRemote() throws Exception {
        TimeInterval timeInterval = new TimeInterval();

        log.info("################开始执行[{}]类的方法[initDataFromRemote]###################",
                this.getClass().getSimpleName(),timeInterval.interval());
        log.info("执行初始化表数据...");
        //初始化表
        initTable();
        //从远程加载集合对象，并简单处理
        log.info("执行读取远程数据...");
        List list = readFileFromRemote();
        //放入数据库
        log.info("执行初始化批量更新操作...");
        initBatchUpdate(list);
        log.info("################结束执行[{}]类的方法[initDataFromRemote], 共花费【{}】毫秒###################",
                this.getClass().getSimpleName(),timeInterval.interval());
    }

    /**
     * 比较并更新
     *
     * 1. 读取远程数据
     * 2. 读取DB数据
     * 3. 执行对比和更新
     * @throws Exception
     */
    public void compareAndUpdate() throws Exception{
        TimeInterval timeInterval = new TimeInterval();
        log.info("################开始执行[{}]类的方法[compareAndUpdate]###################",this.getClass().getName());
        executeCompareAndUpdate(readFileFromRemote(),loadTodayData());
        log.info("################结束执行[{}]类的方法[compareAndUpdate], 共花费【{}】毫秒###################",
                this.getClass().getName(),timeInterval.interval());

    }

    /**
     * 对比和执行
     * @param list
     * @param obj
     * @throws IOException
     */
    protected abstract void executeCompareAndUpdate(List list, Object obj) throws IOException;

    /**
     * 获取今天的数据
     * @throws IOException
     * @return
     */
    protected abstract Object loadTodayData() throws IOException;

    /**
     * 下载文件
     */
//    public  void downloadFile2Local() throws IOException{
//
//    }

    /**
     * 读取本地文件
     * @throws IOException
     */
    protected abstract List readFileFromLocal() throws IOException;

    /**
     * 从远程读取数据
     * @throws IOException
     */
    protected abstract List readFileFromRemote() throws IOException;

    /**
     * 放数据到redis
     * @param ncovList
     */
    public void putDataInRedis(List ncovList){

    }

    /**
     * 初始化表， 如truncate table
     * @
     */
    protected abstract void initTable();

    /**
     * 批量插入数据到数据库
     * @
     */
    protected abstract void initBatchUpdate(List ncovList);




}
