package com.example.service.service;

import com.example.service.pattern.singleton.SingletonClass;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PatternService {
    /**
     * Singleton: đảm bảo 1 class chỉ có 1 instance
     * @return
     */
    public String executeSingleton() {
        SingletonClass instance = SingletonClass.getInstance();
        SingletonClass newInstance = SingletonClass.getInstance();
        if(instance.equals(newInstance)) {
            return "Singleton Instance Matched";
        }
        return "Singleton Instance Not Matched";
    }


    public void executeObserver() {

    }

    public String transform(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Define the pattern to match ${...}
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();

        // Find and replace matches with uppercase content
        while (matcher.find()) {
            String matchedGroup = matcher.group(1); // Get the content inside ${...}
            matcher.appendReplacement(result, "\\$\\{" + matchedGroup.toUpperCase() + "\\}");
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
