package com.tree.ncov.redis;

import java.util.Set;

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
public interface ISetRedisService extends IRedisService{

    Set<Object> getSet(String key);

    Set<Object> getZset(String key, long start, long end);
}
