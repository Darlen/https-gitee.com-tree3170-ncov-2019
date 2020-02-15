package com.tree.ncov.redis;

import java.util.List;

/**
 * @ClassName com.demo.common.redis
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2019-05-26 13:25
 * @Version 1.0
 */
public interface IListRedisService extends IRedisService{

    List<Object> getList(String key, long start, long end);

}
