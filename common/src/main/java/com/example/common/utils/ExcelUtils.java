package com.example.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    public static List<Map<String, Object>> extractDataFromExcelWithHeader(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, Object> dataMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    dataMap.put(headers.get(j), value);
                }
                dataList.add(dataMap);
            }
            return dataList.stream()
                    .filter(map -> map.values().stream().anyMatch(value -> value != null && !value.toString().trim().isEmpty()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("[EXCEL-WITH-HEADER-PROCESS][{}] ERROR: {}", file.getOriginalFilename(), e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static Map<Integer, List<Object>> extractDataFromExcelWithoutHeader(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            Map<Integer, List<Object>> data = new HashMap<>();
            int i = 0;
            for (Row row : sheet) {
                data.put(i, new ArrayList<Object>());
                for (Cell cell : row) {
                    getCellValue(cell);
                }
                i++;
            }
            return data;
        } catch (IOException e) {
            log.error("[EXCEL-WITHOUT-HEADER-PROCESS][{}] ERROR: {}", file.getOriginalFilename(), e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static Object getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

}
