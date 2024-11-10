package com.example.service.service;

import com.example.common.utils.ExcelUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.StringUtils;
import com.example.service.model.file.request.UploadFileRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.common.utils.StringUtils.convertKeysToCamelCase;

@Service
public class FileService {
    public void upload(UploadFileRequest request) {

        List<Map<String, Object>> data = extract(request);
        List<com.example.service.model.Service> services = convertToObject(data, com.example.service.model.Service.class);
        writeSQLToFile(services);
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
                    throw new RuntimeException(e);
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
