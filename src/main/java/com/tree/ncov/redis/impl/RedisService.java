package com.tree.ncov.redis.impl;

import com.alibaba.fastjson.JSON;
import com.tree.ncov.redis.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName com.demo.common.redis.impl
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2019-05-26 13:16
 * @Version 1.0
 */
public class RedisService implements IRedisService, IHashRedisService, IStringRedisService, IListRedisService, ISetRedisService {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void hashSet(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key,map);

    }

    @Override
    public Object hashGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key,hashKey);
    }

    @Override
    public boolean hashExists(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key,hashKey);
    }

    @Override
    public List<Object> hashMultiGet(String key, List<Object> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key,hashKeys);
    }

    @Override
    public Map<Object, Object> getHashEntries(String hash) {
        return redisTemplate.opsForHash().entries(hash);
    }

    @Override
    public Set<Object> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Set<Object> getZset(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key,start,end);
    }

    @Override
    public List<Object> getList(String key, long start, long end) {
        return redisTemplate.opsForList().range(key,start,end);
    }

    @Override
    public String getString(String key) {
        return (String)redisTemplate.opsForValue().get(key);
    }

    @Override
    public Set<Object> getKeysByPrefix(String prefixKey) {
        return redisTemplate.keys(prefixKey+"*");
    }

    @Override
    public Set<Object> scanKeysByPrefix(String prefixKey ) {
        Set<Object> set = (Set<Object>) redisTemplate.execute((RedisCallback<Set<Object>>) connection -> {
           Set<Object> binaryKeys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(prefixKey+"*").count(1000).build());
            while (cursor.hasNext()){
                binaryKeys.add(new String(cursor.next()));
            }
            return binaryKeys;
        });

        return set;
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public DataType getValueType(String key) {
        return redisTemplate.type(key);
    }

    @Override
    public void put(String key, Object value, int seconds) {
        redisTemplate.opsForValue().set(key,value,seconds,TimeUnit.SECONDS);
    }

    @Override
    public void put(String key, String value, int seconds) {
        redisTemplate.opsForValue().set(key,value,seconds,TimeUnit.SECONDS);
    }

    @Override
    public void put(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key,value,timeout,unit);

    }

    @Override
    public void expire(String key, int seconds) {
        redisTemplate.expire(key,seconds,TimeUnit.SECONDS);
    }

    @Override
    public void expireAt(String key, Long timestamps) {
        Date date = new Date(timestamps);
        redisTemplate.expireAt(key,date);
    }



    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void removeKeys(Set<Object> keys) {
        redisTemplate.delete(keys);
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        String obj = (String) redisTemplate.opsForValue().get(key);
        return JSON.parseObject(obj,clazz);
    }
}
