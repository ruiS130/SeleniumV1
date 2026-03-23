package com.automation;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {

    // Reads a row from Excel
    // columnNames = the header row values you want to read
    public static Map<String, String> getTestData(String filePath, String sheetName) throws IOException {
        Map<String, String> data = new HashMap<>();

        FileInputStream file = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheet(sheetName);

        // Reads from Row 1 where actual data is
        Row headers = sheet.getRow(0);
        Row values = sheet.getRow(1);

        // Using the for loop to store the row 0 as a key-value map
        for(int i = 0; i < headers.getLastCellNum(); i++) {
            String key = headers.getCell(i).getStringCellValue();
            String value = values.getCell(i).getStringCellValue();
            data.put(key, value);
        }

        workbook.close();
        file.close();
        return data;
    }
}