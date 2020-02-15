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
public interface IStringRedisService extends IRedisService{

    String getString(String key);

    Set<Object> getKeysByPrefix(String prefixKey);

    Set<Object> scanKeysByPrefix(String prefixKey);
}
