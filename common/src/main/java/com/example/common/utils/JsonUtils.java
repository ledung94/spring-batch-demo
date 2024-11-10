package com.example.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonUtils {

    public static Map toJson (Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(obj, Map.class);
    }

    public static Object toObject (Map json, Class<?> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(json, clazz);
    }

    @SneakyThrows
    public static Object buildObject(Map<String, Object> json, Class<?> clazz) {
        Set<String> fieldNames = Stream.of(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        return toObject(json.entrySet().stream()
                .filter(entry -> fieldNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), clazz);
    }

    public static Map mergeJson(Map originMap, Map targerMap) {
        Map<String, Integer> result = new HashMap<>(originMap); // Sao chép map1 vào result
        result.putAll(targerMap); // Thêm tất cả các mục từ map2 vào result (ghi đè nếu có khóa trùng lặp)
        return result;
    }
}
