package com.tree.ncov.redis;

import org.springframework.data.redis.connection.DataType;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName com.demo.common.redis
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2019-05-26 13:13
 * @Version 1.0
 */
public interface IRedisService {

    Object get(String key);

    DataType getValueType(String key);

    void put(String key, Object value, int seconds);

    void put(String key, String value, int seconds);

    void put(String key, String value, long timeout, TimeUnit unit);

    void expire(String key, int seconds);

    void expireAt(String key, Long timestamps);


    boolean exists(String key);

    void remove(String key);

    void removeKeys(Set<Object> keys);

    <T> T getObject(String key, Class<T> clazz);

}
