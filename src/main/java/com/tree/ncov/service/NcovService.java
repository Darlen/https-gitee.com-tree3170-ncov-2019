package com.tree.ncov.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @ClassName com.tree.ncov
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-12 21:23
 * @Version 1.0
 */
@Component
public class NcovService {
    @Autowired
    private RedisTemplate redisTemplate;
    int i= 20200212;
    public void setValue(){

        redisTemplate.opsForValue().set("test_"+i,i+ LocalDateTime.now().getNano());
    }

    public Object getValue(){
        return redisTemplate.opsForValue().get("test_"+i);
    }
}
