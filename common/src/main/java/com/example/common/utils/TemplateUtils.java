package com.example.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtils {

    public static String buildMessage (String content, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }

        return content;
    }

    static public List<String> getMatches(String content) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile("(\\$\\{)(.*?)\\}")
                .matcher(content);
        while (m.find()) {
            allMatches.add(m.group());
        }

        List<String> results = new ArrayList<>();
        for (String result: allMatches) {
            String r = result.substring(2, result.length()-1);
            results.add(r);
        }

        return results;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || Objects.equals(str, "null");
    }
    
}