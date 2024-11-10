package com.example.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isMatch(String str, String pattern) {
        if (isEmpty(str) || isEmpty(pattern)) {
            return false;
        }
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(str);
        return matcher.matches();
    }

    public static boolean isTrue(Object input) {
        return Objects.nonNull(input) && List.of("TRUE", "0").contains(input.toString().toUpperCase());
    }

    public static String snakeToCamel(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean toUpperCase = false;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                toUpperCase = true;
            } else if (toUpperCase) {
                result.append(Character.toUpperCase(c));
                toUpperCase = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    // Chuyển Map<String, Object> với key snake_case sang camelCase
    public static Map<String, Object> convertKeysToCamelCase(Map<String, Object> map) {
        Map<String, Object> camelCaseMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String camelCaseKey = snakeToCamel(entry.getKey());
            camelCaseMap.put(camelCaseKey, entry.getValue());
        }
        return camelCaseMap;
    }

    public static String generateInsertSQL(Object obj) {
        try {
            StringBuilder sql = new StringBuilder("INSERT INTO ");

            // Lấy tên của class (bảng) từ tên của đối tượng
            String tableName = obj.getClass().getSimpleName().toUpperCase();
            sql.append(tableName).append(" (");

            StringBuilder values = new StringBuilder("VALUES (");
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);

                // Thêm tên cột vào câu lệnh SQL
                sql.append(field.getName().toUpperCase()).append(", ");

                // Thêm giá trị vào câu lệnh SQL, xử lý các kiểu dữ liệu khác nhau
                if ("id".equals(field.getName())) {
                    values.append(value).append(", ");
                    continue;
                }
                if (value instanceof String) {
                    values.append("'").append(value).append("', ");
                } else if (value == null) {
                    values.append("NULL, ");
                } else {
                    values.append(value).append(", ");
                }
            }

            // Xóa dấu phẩy cuối cùng và đóng ngoặc
            sql.setLength(sql.length() - 2); // Xóa dấu ", " cuối
            values.setLength(values.length() - 2); // Xóa dấu ", " cuối
            sql.append(") ").append(values).append(")");

            return sql.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
