package com.example.service.service;

import com.example.common.utils.ExcelUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.StringUtils;
import com.example.service.model.file.request.UploadFileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.common.utils.StringUtils.convertKeysToCamelCase;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public void upload(UploadFileRequest request) {
        try {
            MultipartFile file = request.getFile();
            List<Map<String, Object>> data = extract(request);
            String className = StringUtils.extractClassName(file.getOriginalFilename());
            List objects = convertToObject(data, StringUtils.findClassByName(className));
            writeSQLToFile(objects);
        } catch (ClassNotFoundException e) {
            log.error("FileHandle | Error | {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> extract(UploadFileRequest request) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (request.isHasHeader()) {
            data = ExcelUtils.extractDataFromExcelWithHeader(request.getFile());
        } else {
//            data = ExcelUtils.extractDataFromExcelWithoutHeader(request.getFile());
        }
        return data;
    }

    private <T> List<T> convertToObject(List<Map<String, Object>> data, Class<T> clazz)  {
        List<T> result = new ArrayList<>();
        for (Map<String, Object> map : data) {
            Map<String, Object> camelCaseMap = convertKeysToCamelCase(map);
            T object = null;
            try {
                object = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            for (Map.Entry<String, Object> entry : camelCaseMap.entrySet()) {
                Field field = null;
                try {
                    field = clazz.getDeclaredField(entry.getKey());
                } catch (NoSuchFieldException e) {
                    log.error("Failed to get field {} | error : {}", entry.getKey(), e.getMessage());
                    continue;
                }
                field.setAccessible(true); // Cho phép truy cập vào các field private
                try {
                    field.set(object, entry.getValue());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            result.add(object);
        }
        return result;
    }

    private <T> File writeSQLToFile(List<T> list) {
        List<String> sql = new ArrayList<>();
        for (T item : list) {
            sql.add(StringUtils.generateInsertSQL(item));
        }
        return FileUtils.writeToFile(sql, "output.txt");
    }
}
