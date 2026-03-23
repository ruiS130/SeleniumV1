package com.automation;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {

    // Takes a screenshot and saves it to screenshots/scenarioName/stepName_timestamp.png
    public static String takeScreenshot(WebDriver driver, String scenarioName, String stepName) throws IOException {
        // Create folder for this scenario if it doesn't exist
        String folderPath = "screenshots/" + scenarioName;
        new File(folderPath).mkdirs();

        // Add timestamp so screenshots don't overwrite each other
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = folderPath + "/" + stepName + "_" + timestamp + ".png";

        // Take and save the screenshot
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(screenshot, new File(filePath));
        return filePath; // return path so ExtentReports can attach it later
    }
}