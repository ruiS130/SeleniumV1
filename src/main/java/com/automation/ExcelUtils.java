package com.automation;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ExcelUtils {

    // Reads a row from Excel
    // columnNames = the header row values you want to read
    public static Map<String, String> getTestData(String filePath, String sheetName) throws IOException {
        Map<String, String> data = new HashMap<>();

        FileInputStream file = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheet(sheetName);

        // Reads from Row 0 and Row 1, where actual data is
        Row headers = sheet.getRow(0);
        Row values = sheet.getRow(1);

        // Using the for loop to store the data read as a key-value map
        for(int i = 0; i < headers.getLastCellNum(); i++) {
            String key = headers.getCell(i).getStringCellValue();
            String value = values.getCell(i).getStringCellValue();
            data.put(key, value);
        }

        workbook.close();
        file.close();
        return data;
    }

    public static String decodePassword(String encoded) {
        return new String(Base64.getDecoder().decode(encoded.trim())).trim();
    }

    public static List<Map<String, String>> getEventData(String filePath, String sheetName) throws IOException {
        List<Map<String, String>> events = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        FileInputStream file = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheet(sheetName);

        Row headers = sheet.getRow(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, String> event = new HashMap<>();
            for (int j = 0; j < headers.getLastCellNum(); j++) {
                String key = headers.getCell(j).getStringCellValue().trim();
                Cell cell = row.getCell(j);
                String value = (cell != null) ? formatter.formatCellValue(cell).trim() : "";
                event.put(key, value);
            }
            events.add(event);
        }

        workbook.close();
        file.close();
        return events;
    }
}