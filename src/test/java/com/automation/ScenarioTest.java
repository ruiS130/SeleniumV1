package com.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Listeners
public class ScenarioTest {

    WebDriver driver;
    WebDriverWait wait;
    Map<String, String> testData;
    static final String SCENARIO_NAME = "Scenario1_Download_Transcript";

    @BeforeClass
    public void setUp() throws Exception {
        // Clean up old PDFs
        File pdfFolder = new File("screenshots/" + SCENARIO_NAME);
        if (pdfFolder.exists()) {
            for (File file : Objects.requireNonNull(pdfFolder.listFiles())) {
                if (file.getName().endsWith(".pdf")) {
                    file.delete();
                }
            }
        }

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

    @Test(priority = 1)
    public void scenario1_downloadTranscript() throws Exception {

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
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "03a_password_entered");
        driver.findElement(By.id("idSIButton9")).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "03b_after_login_click");

        // Step 3: Wait for Duo approval, then click "No, other people use this device"
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("dont-trust-browser-button"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "03a_after_duo");

        // Reset wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Click "No" to stay signed in prompt
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("idBtn_Back"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "03b_stay_signed_in_no");

        // Step 4: Click Resources tab
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='link-resources']"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "04_resources_tab");

        // Step 5: Click Academics, Classes & Registration icon
        WebElement academicsIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("img[src*='academicsclassesregistration']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", academicsIcon);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "05_academics");

        // Dismiss cookie banner if present
        try {
            WebElement cookieBanner = driver.findElement(By.id("truste-consent-track"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", cookieBanner);
        } catch (NoSuchElementException e) {
            // Banner not present, continue
        }

        // Step 6: Click Unofficial Transcript
        WebElement transcript = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[data-gtm-resources-link='Unofficial Transcript']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", transcript);

        // Switch to the new tab
        String originalWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).sendKeys(testData.get("nuid_username"));

        driver.findElement(By.id("password")).sendKeys(ExcelUtils.decodePassword(testData.get("password")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "06a_credentials_entered");

        driver.findElement(By.name("_eventId_proceed")).click();

        // Switch into Duo iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("duo_iframe")));

        // Click "Send Me a Push"
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.auth-button"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "06b_send_push");

        // Wait for DUO approval, click "No, other people use this device" (if present)
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("dont-trust-browser-button"))).click();
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "06c_duo_transcript");
        } catch (TimeoutException e) {
            System.out.println("Trust browser prompt did not appear, skipping...");
        }

        // Switch back out
        driver.switchTo().defaultContent();
        // Step 7: Click "Transcript Level" dropdown and select Graduate
        wait.until(ExpectedConditions.elementToBeClickable(By.id("select2-placeholder-1"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'select2-result') and contains(.,'Graduate')]"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "7a_graduate_selected");

        // Click "Transcript Type" dropdown and select Audit Transcript
        wait.until(ExpectedConditions.elementToBeClickable(By.id("select2-placeholder-2"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'select2-result') and contains(.,'Audit Transcript')]"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "7b_audit_selected");

        // Submit (if there's a submit button, click it first)
        // driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Step 8: Wait for transcript to load, then print to PDF
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        Thread.sleep(2000); // give it a moment to fully render
        ((JavascriptExecutor) driver).executeScript("window.print();");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_NAME, "8_after_print");
        // Assert transcript page loaded successfully
        Assert.assertFalse(driver.getTitle().isEmpty(), "Page title should not be empty");

        Assert.assertTrue(driver.getTitle().contains("Transcript"),
                "Expected transcript page, got: " + driver.getTitle());
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}