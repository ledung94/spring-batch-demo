package com.example.common.utils;

import com.example.common.model.Mapping;
import com.google.gson.Gson;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingUtils {
    public static final Gson gson = new Gson();

    public static <T> Map<String, T> buildMapData(JSONObject object, List<Mapping> configParams,
                                                  Class<T> clazz) {
        Map<String, T> mapData = new HashMap<>();
        if (CollectionUtils.isEmpty(configParams))
            return mapData;

        for (Mapping item : configParams) {
            try {
                Object value = StringUtils.hasText(item.getDefaultValue()) ? item.getDefaultValue()
                        : findValueByPath(object, item.getFromProperty());
                if (Objects.nonNull(value)) {
                    mapData.put(item.getToProperty(), clazz.cast(value));
                }
            } catch (Exception e) {
                log.error("buildMapData | error processing item {} | {}", item, e.getMessage(), e);
            }
        }
        return mapData;
    }

    public static JSONObject buildJsonData(JSONObject object, List<Mapping> configParams) {
        JSONObject output = new JSONObject();
        if (CollectionUtils.isEmpty(configParams))
            return output;

        for (Mapping item : configParams) {
            try {
                Object value = StringUtils.hasText(item.getDefaultValue())
                        ? formatValue(item.getDefaultValue(), item.getDataType(), item.getFormat())
                        : formatValue(findValueByPath(object, item.getFromProperty()), item.getDataType(),
                        item.getFormat());
                if (Objects.nonNull(value)) {
                    setValueToObject(output, item.getToProperty(), value);
                }
            } catch (Exception e) {
                log.error("buildJsonData | error processing item {} | {}", item, e.getMessage(), e);
            }
        }
        return output;
    }

    @SneakyThrows
    private static Object findValueByPath(JSONObject jsonObject, String path) {
        if (!StringUtils.hasText(path)) return null;
        String[] keys = path.split("\\.");

        JSONObject currentObject = jsonObject;

        for (int i = 0; i < keys.length - 1; i++) {
            currentObject = currentObject.optJSONObject(keys[i]);
            if (currentObject == null) return null;
        }

        String lastKey = keys[keys.length - 1];

        if (!currentObject.has(lastKey)) return null;

        Object value = currentObject.get(lastKey);

        if (value instanceof JSONObject || value instanceof JSONArray) {
            return gson.fromJson(value.toString(), value instanceof JSONArray ? List.class : Object.class);
        }

        return value;
    }

    @SneakyThrows
    private static void setValueToObject(JSONObject jsonObject, String key, Object value) {
        List<String> keyTree = Arrays.asList(key.split("\\."));
        JSONObject currentObject = jsonObject;

        for (int i = 0; i < keyTree.size() - 1; i++) {
            String node = keyTree.get(i);
            if (!currentObject.has(node) || currentObject.isNull(node)) {
                currentObject.put(node, new JSONObject());
            }

            Object current = currentObject.get(node);
            if (current instanceof JSONArray jsonArray) {
                if (jsonArray.isEmpty()) {
                    jsonArray.put(new JSONObject());
                }
                currentObject = jsonArray.getJSONObject(0);
            } else if (current instanceof JSONObject) {
                currentObject = (JSONObject) current;
            } else if (current instanceof String) {
                try {
                    JSONObject parsedObject = new JSONObject((String) current);
                    parsedObject.put(keyTree.get(i + 1), value);
                    currentObject.put(node, parsedObject.toString());
                } catch (Exception e) {
                    log.info("setValueToObject | key: {} | error processing item: {} | {}", key, current, e.getMessage());
                }
                return;
            } else {
                currentObject.put(node, new JSONObject());
                currentObject = currentObject.getJSONObject(node);
            }
        }

        currentObject.put(keyTree.get(keyTree.size() - 1), value);
    }

    @SneakyThrows
    private static Object formatValue(Object value, String dataType, String format) {
        if (!org.springframework.util.StringUtils.hasText(dataType)) {
            return value;
        }
        if ("JsonArray".equalsIgnoreCase(dataType))
            return new JSONArray();
        if ("JsonObject".equalsIgnoreCase(dataType))
            return new JSONObject();
        if ("JsonString".equalsIgnoreCase(dataType))
            return Objects.isNull(value) || "null".equalsIgnoreCase(value.toString()) ? new JSONObject().toString() : value.toString();
        if (Objects.isNull(value) || !org.springframework.util.StringUtils.hasText(value.toString())) {
            return null;
        }
        return switch (dataType.toUpperCase()) {
            case "DATETIME" -> {
                if (!StringUtils.hasText(format))
                    yield value;
                yield new SimpleDateFormat(format)
                        .format(new SimpleDateFormat("yyyyMMddHHmmss").parse(value.toString()));
            }
            case "INTEGER" -> Integer.parseInt(value.toString());
            case "LONG" -> Long.parseLong(value.toString());
            case "FLOAT" -> Float.parseFloat(value.toString());
            case "BIGDECIMAL" -> new BigDecimal(value.toString());
            default -> value;
        };
    }
}
