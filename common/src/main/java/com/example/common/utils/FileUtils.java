package com.example.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static File writeToFile(List<String> values, String filePath)  {
        try {
            File file = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String sql : values) {
                    writer.write(sql);
                    writer.newLine();  // Ghi xuống dòng sau mỗi câu SQL
                }
            }
            return file;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
