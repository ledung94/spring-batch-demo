package com.example.common.utils;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RedisUtils {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisUtils(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SneakyThrows
    public void saveObjectByKey(String key, Object obj) {
        String json = (new Gson()).toJson(obj);
        this.redisTemplate.opsForValue().set(key, json);
    }

    @SneakyThrows
    public Object findObjectByKey(String key, Class clazz) {
        String jsonObj = this.redisTemplate.opsForValue().get(key);
        Gson gson = new Gson();
        if(!StringUtils.hasText(jsonObj))
            return null;
        Object obj = gson.fromJson(jsonObj, clazz);
        return obj;
    }

    @SneakyThrows
    public void deleteObjByKey(String key) {
        this.redisTemplate.opsForHash().getOperations().delete(key);
    }
}
