package com.example.service.service;

import com.example.service.entity.ApiMapper;
import com.example.service.model.partner.request.TransactionRequestDTO;
import com.example.service.model.partner.response.PartnerNotificationResponseDTO;
import com.example.service.repository.ApiMapperRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kong.unirest.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class TransactionIdentifyService {
    @Autowired
    ApiMapperRepository apiMapperRepository;

    public boolean validate(TransactionRequestDTO request) {
        return true;
    }

    public ResponseEntity<PartnerNotificationResponseDTO> process(TransactionRequestDTO request) {
        JSONObject jsonObject = buildJsonObjectForMapping(request);
        List<ApiMapper> smsProviderMappers = apiMapperRepository.findAllByPartnerIdOrderByOrder(1L);

        JsonObject body = buildBodyInfo(smsProviderMappers, jsonObject);
        HttpHeaders headers = buildHeaders(smsProviderMappers, jsonObject);
        buildSignature(smsProviderMappers, jsonObject, body, headers);
        return null;
    }

    private HttpHeaders buildHeaders(List<ApiMapper> smsProviderMappers, JSONObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        for (ApiMapper item : smsProviderMappers) {
            if(!BooleanUtils.toBoolean(item.getIsHeader()) || BooleanUtils.toBoolean(item.getIsSignature())) continue;
            String value = Objects.isNull(item.getDefaultValue())
                    ? Objects.isNull(jsonObject.get(item.getFromProperty())) ? null : String.valueOf(jsonObject.get(item.getFromProperty()))
                    : item.getDefaultValue();
            headers.add(item.getToProperty(), value);
        }
        return headers;
    }

    private JsonObject buildBodyInfo(List<ApiMapper> smsProviderMappers, JSONObject jsonObject) {
        JsonObject body = new JsonObject();
        for (ApiMapper item : smsProviderMappers) {
            if(!BooleanUtils.toBoolean(item.getIsBody()) || BooleanUtils.toBoolean(item.getIsSignature())) continue;

            String value = Objects.isNull(item.getDefaultValue())
                    ? Objects.isNull(item.getFromProperty()) || Objects.isNull(jsonObject.get(item.getFromProperty())) ? null : String.valueOf(jsonObject.get(item.getFromProperty()))
                    : item.getDefaultValue();

            // body -> item
            if (Objects.isNull(item.getParentName())) {
                addProperty(body, item.getToProperty(), value, item.getDataType());
            }
            // body -> ... parent -> item
            else {
                String parentType = smsProviderMappers.stream()
                        .filter(ele -> ele.getToProperty().equals(item.getParentName()))
                        .findFirst().map(ApiMapper::getDataType).orElse(null);
                JsonObject parent = getJsonObjectValue(body, item.getParentName());
                if (StringUtils.isNotEmpty(parentType) || parent == null) continue;
                addProperty(parent, item.getToProperty(), value, item.getDataType());
            }
        }
        return body;
    }

    /**
     * Get value of parent by key
     **/
    private static JsonObject getJsonObjectValue(JsonObject jsonObject, String targetKey) {
        for (String key : jsonObject.keySet()) {
            JsonElement element = jsonObject.get(key);
            if(key.equals(targetKey)) {
                if(element.isJsonObject()) {
                    return element.getAsJsonObject();
                } else if (element.isJsonArray() && !element.getAsJsonArray().isEmpty()) {
                    return (JsonObject) element.getAsJsonArray().get(0);
                }
            }
            else if (element.isJsonObject()) {
                JsonObject value = getJsonObjectValue(element.getAsJsonObject(), targetKey);
                if (value != null) {
                    return value;
                }
            } else if (element.isJsonArray() && !element.getAsJsonArray().isEmpty()) {
                JsonObject value = getJsonObjectValue((JsonObject) element.getAsJsonArray().get(0), targetKey);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private void buildSignature(List<ApiMapper> smsProviderMappers, JSONObject jsonObject, JsonObject body, HttpHeaders headers) {
        for (ApiMapper item : smsProviderMappers) {
            if(!BooleanUtils.toBoolean(item.getIsSignature())) continue;
            String signature = switch (item.getSignatureType()) {
                case "String" -> buildStringSignature(jsonObject, item);
                case "Json" -> buildJsonSignature(body);
                default -> "";
            };

            if (BooleanUtils.toBoolean(item.getIsBody())) {
                if (Objects.isNull(item.getParentName())) {
                    body.addProperty(item.getToProperty(), signature);
                }
                // Nested Json
                else {
                    JsonObject parent = getJsonObjectValue(body, item.getParentName());
                    if (parent == null) continue;
                    parent.addProperty(item.getToProperty(), signature);
                }
            } else if (BooleanUtils.toBoolean(item.getIsHeader())) {
                headers.add(item.getToProperty(), signature);
            }
        }
    }

    private String buildStringSignature(JSONObject jsonObject, ApiMapper item) {
        List<String> content = List.of(item.getFromProperty().split("\\|"));
        List<String> values = new ArrayList<>();
        for (String string : content) {
            values.add(Objects.isNull(jsonObject.get(string)) ? StringUtils.EMPTY : String.valueOf(jsonObject.get(string)));
        }
        return String.join("|", values);
    }

    private String buildJsonSignature(JsonObject body) {
        return null;
    }

    private void addProperty(JsonObject object, String key, String value, String dataType) {
        switch (dataType) {
            case "Integer":
                object.addProperty(key, NumberUtils.createBigInteger(String.valueOf(value)));
                break;
            case "Long":
                object.addProperty(key, NumberUtils.createLong(value));
                break;
            case "Float":
                object.addProperty(key, NumberUtils.createFloat(value));
                break;
            case "BigDecimal":
                object.addProperty(key, NumberUtils.createBigDecimal(value));
                break;
            case "JsonObject":
                object.add(key, new JsonObject());
                break;
            case "JsonArray":
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(new JsonObject());
                object.add(key, jsonArray);
                break;
            default:
                object.addProperty(key, Optional.ofNullable(value).orElse(StringUtils.EMPTY));
                break;
        }
    }

    public static JSONObject buildJsonObjectForMapping(Object object) {
        JSONObject jsonObject = new JSONObject();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object value = field.get(object);
                jsonObject.put(fieldName, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonObject;
    }
}
