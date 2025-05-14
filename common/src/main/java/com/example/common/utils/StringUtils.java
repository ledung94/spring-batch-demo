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

    public static String toUpperSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static String formatValue(Object input) {
        if (Objects.isNull(input))  return null;
        return input.toString().replaceAll("'([^']+)'", "''$1''");
    }

    public static String generateInsertSQL(Object obj) {
        try {
            String declareStr = "DECLARE \n";
            StringBuilder declareSql = new StringBuilder(declareStr);
            StringBuilder sql = new StringBuilder("INSERT INTO ");

            // Lấy tên của class (bảng) từ tên của đối tượng
            String tableName = obj.getClass().getSimpleName().toUpperCase();
            sql.append("MESSAGING.").append(tableName).append(" (");

            StringBuilder values = new StringBuilder("VALUES (");
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);

                // Thêm tên cột vào câu lệnh SQL
                sql.append(toUpperSnakeCase(field.getName())).append(", ");

                // template content too long -> not set value && continue
                if(List.of("tempContent").contains(field.getName())) {
                    values.append("v_temp_content").append(", ");
                    declareSql.append("v_temp_content CLOB := ").append("'").append(formatValue(value)).append("'").append(";");
                    continue;
                }

                // Thêm giá trị vào câu lệnh SQL, xử lý các kiểu dữ liệu khác nhau
                if (List.of("id", "createTime", "updateTime", "serviceId").contains(field.getName())) {
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
            if(org.springframework.util.StringUtils.hasText(declareSql.toString()) && !declareStr.equals(declareSql.toString())) {
                return declareSql.append("\n BEGIN \n").append(sql).append(";").append("\n END").toString();
            }
            return sql.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return as is if null or empty
        }
        // Capitalize the first letter and concatenate with the rest of the string
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String extractClassName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static Class<?> findClassByName(String className) throws ClassNotFoundException {
        // Replace with the actual package if known
        String basePackage = "com.example.service.model"; // Replace with your base package
        String fullyQualifiedName = basePackage + "." + capitalizeFirstLetter(className);

        // Load the class dynamically
        return Class.forName(fullyQualifiedName);
    }
}
