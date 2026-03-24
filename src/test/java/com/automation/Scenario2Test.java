package com.automation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.util.Map;

public class Scenario2Test {
    WebDriver driver;
    WebDriverWait wait;
    Map<String, String> testData;
    static final String SCENARIO_NAME = "Scenario1_Download_Transcript";

    @Test
    public void eventCreation() throws Exception {
        driver.get("https://canvas.northeastern.edu");
    }

}
