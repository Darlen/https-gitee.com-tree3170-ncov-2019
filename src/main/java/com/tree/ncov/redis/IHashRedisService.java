package com.tree.ncov.redis;

import java.util.List;
import java.util.Map;

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
public interface IHashRedisService extends IRedisService{

    void hashSet(String key, Map<String, Object> map);

    Object hashGet(String key, Object hashKey);

    boolean hashExists(String key, Object hashKey);

    List<Object> hashMultiGet(String key, List<Object> hashKeys);

    Map<Object, Object> getHashEntries(String hash);


}
