package com.example.common.utils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
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
}
