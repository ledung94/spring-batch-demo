package com.example.common.utils;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class CacheUtils {
    private final RedisUtils redisUtils;

    public CacheUtils(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public <T> List<T> getFromCache(String redisKey, Class<T> clazz, Predicate<T> filter, Function<String, List<T>> fetcher) {
        List<T> items = new ArrayList<>();
        // Get the list of items from Redis cache
        List<LinkedTreeMap<String, String>> treeMaps = (List<LinkedTreeMap<String, String>>) redisUtils.findObjectByKey(redisKey, List.class);
        if (!CollectionUtils.isEmpty(treeMaps)) {
            for (LinkedTreeMap<String, String> tempItem : treeMaps) {
                String json = new Gson().toJson(tempItem);
                Gson g = new Gson();
                T item = g.fromJson(json, clazz);
                items.add(item);
            }
        }
        // If the cache is empty, fetch from the source
        if (CollectionUtils.isEmpty(items)) {
            items = fetcher.apply(redisKey);
        }
        // Filter the items if a filter is provided
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        return filter != null ?
                items.stream().filter(filter).collect(Collectors.toList()) :
                items;
    }
}
