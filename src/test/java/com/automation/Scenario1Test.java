package com.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Scenario1Test {

    WebDriver driver;
    WebDriverWait wait;
    Map<String, String> testData;
    static final String SCENARIO_NAME = "Scenario1_Download_Transcript";

    @BeforeClass
    public void setUp() throws Exception {
        // Read credentials from Excel
        testData = ExcelUtils.getTestData("testdata.xlsx", "Sheet1");
//        System.out.println("Decoded password: [" + ExcelUtils.decodePassword(testData.get("password")) + "]");

        // Configure Chrome to auto-save PDFs instead of showing print dialog
        ChromeOptions options = getChromeOptions();
        options.addArguments("--kiosk-printing"); // auto-confirm print dialog

//        WebDriverManager.chromedriver().setup();
        WebDriverManager.chromedriver().clearDriverCache().setup();

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    private static @NonNull ChromeOptions getChromeOptions() {
        Map<String, Object> prefs = new HashMap<>();
        String pdfFolder = new File("screenshots/" + SCENARIO_NAME).getAbsolutePath();
        new File(pdfFolder).mkdirs();
        prefs.put("savefile.default_directory", pdfFolder);
        prefs.put("printing.print_preview_sticky_settings.appState",
                "{\"recentDestinations\":[{\"id\":\"Save as PDF\",\"origin\":\"local\",\"account\":\"\"}]," +
                        "\"selectedDestinationId\":\"Save as PDF\",\"version\":2}");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    @Test
    public void downloadTranscript() throws Exception {

        // Step 1: Open NEU login page
        driver.get("https://me.northeastern.edu");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "01_before_login");

        // Step 2: Enter username
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        driver.findElement(By.id("i0116")).sendKeys(testData.get("username"));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "02_username_entered");
        driver.findElement(By.id("idSIButton9")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118")));
        driver.findElement(By.id("i0118")).sendKeys(ExcelUtils.decodePassword(testData.get("password")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "03_password_entered");
        driver.findElement(By.id("idSIButton9")).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "04_after_login_click");

        // Step 3?: Wait for Duo
//         Thread.sleep(30000); // 30 seconds to approve Duo on your phone
// Step 4: Wait for Duo approval, then click "No, other people use this device"
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("dont-trust-browser-button"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "04_after_duo");

        // Reset wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Click "No" to stay signed in prompt
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("idBtn_Back"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "04c_stay_signed_in_no");

        // Step 5: Click Resources tab
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='link-resources']"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "05_resources_tab");

        // Step 6: Click Academics, Classes & Registration icon
        WebElement academicsIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("img[src*='academicsclassesregistration']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", academicsIcon);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "06_academics");

        // Step 7: Dismiss cookie banner if present
        try {
            WebElement cookieBanner = driver.findElement(By.id("truste-consent-track"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", cookieBanner);
        } catch (NoSuchElementException e) {
            // Banner not present, continue
        }

        // Click Unofficial Transcript
        WebElement transcript = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[data-gtm-resources-link='Unofficial Transcript']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", transcript);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "07_unofficial_transcript");

        // Step 9: Select Graduate in Transcript Level
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("levl_id"))); // we may need to update this ID after inspecting
        Select transcriptLevel = new Select(driver.findElement(By.id("levl_id")));
        transcriptLevel.selectByVisibleText("Graduate");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "09_graduate_selected");

        // Step 10: Click Submit
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "10_after_submit");

        // Step 11: Print to PDF
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "11_before_print");
        ((JavascriptExecutor) driver).executeScript("window.print();");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "12_after_print");

        // Assert transcript page loaded successfully
        Assert.assertFalse(driver.getTitle().isEmpty(), "Page title should not be empty");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}