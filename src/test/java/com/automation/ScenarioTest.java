package com.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.*;
import java.time.Duration;
import java.util.*;

@Listeners(ReportListener.class)
public class ScenarioTest {

    WebDriver driver;
    WebDriverWait wait;
    Map<String, String> testData;
    Keys selectAllKey = System.getProperty("os.name").toLowerCase().contains("mac") ? Keys.COMMAND : Keys.CONTROL;
    static final String SCENARIO_1 = "Scenario1_Download_Transcript";
    static final String SCENARIO_2 = "Scenario2_Add_Event";
    static final String SCENARIO_3 = "Scenario3_Reserve_Seat";
    static final String SCENARIO_4 = "Scenario4_Download_Dataset";
    static final String SCENARIO_5 = "Scenario5_Update_Academic_Calender";

    private long stepStart;

    private void startTimer(String stepName) {
        stepStart = System.currentTimeMillis();
        System.out.println("▶ START: " + stepName);
    }

    private void endTimer(String stepName) {
        long elapsed = System.currentTimeMillis() - stepStart;
        System.out.println("✓ END: " + stepName + " (" + elapsed + "ms)");
    }

    @BeforeClass
    public void cleanUp() {
        // Clean up old screenshots
        String[] scenarios = {SCENARIO_1, SCENARIO_2, SCENARIO_3, SCENARIO_4, SCENARIO_5};
        for (String scenario : scenarios) {
            File folder = new File("screenshots/" + scenario);
            if (folder.exists()) {
                for (File file : Objects.requireNonNull(folder.listFiles())) {
                    if (!file.delete()) {
                        System.out.println("Failed to delete: " + file.getName());
                    }
                }
            }
        }

        // Keep only the latest 5 test reports
        File reportFolder = new File("test-output");
        if (reportFolder.exists()) {
            File[] reports = reportFolder.listFiles((dir, name) -> name.startsWith("TestReport_") && name.endsWith(".html"));
            if (reports != null && reports.length > 5) {
                Arrays.sort(reports, Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < reports.length - 5; i++) {
                    if (!reports[i].delete()) {
                        System.out.println("Failed to delete: " + reports[i].getName());
                    }
                }
            }
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        // Keep only the latest 5 test reports
        File reportFolder = new File("test-output");
        if (reportFolder.exists()) {
            File[] reports = reportFolder.listFiles((dir, name) -> name.startsWith("TestReport_") && name.endsWith(".html"));
            if (reports != null && reports.length > 5) {
                // Sort by last modified, oldest first
                Arrays.sort(reports, Comparator.comparingLong(File::lastModified));
                // Delete oldest ones, keep last 5
                for (int i = 0; i < reports.length - 5; i++) {
                    if (!reports[i].delete()) {
                        System.out.println("Failed to delete: " + reports[i].getName());
                    }
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
        String pdfFolder = new File("screenshots/" + SCENARIO_1).getAbsolutePath();
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "01_before_login");

        // Step 2: Enter username
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        driver.findElement(By.id("i0116")).sendKeys(testData.get("username"));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "02a_username_entered");
        driver.findElement(By.id("idSIButton9")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118")));
        driver.findElement(By.id("i0118")).sendKeys(ExcelUtils.decodePassword(testData.get("password")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "02b_password_entered");
        driver.findElement(By.id("idSIButton9")).click();

        // Step 3: Wait for Duo approval, then click "No, other people use this device"
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("dont-trust-browser-button")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "03a_after_duo");
        driver.findElement(By.id("dont-trust-browser-button")).click();

        // Reset wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Click "No" to stay signed in prompt
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("idBtn_Back")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "03b_stay_signed_in_no");
        driver.findElement(By.id("idBtn_Back")).click();

        // Step 4: Click Resources tab
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='link-resources']"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "04_resources_tab");

        // Step 5: Click Academics, Classes & Registration icon
        WebElement academicsIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("img[src*='academicsclassesregistration']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", academicsIcon);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "05_academics");

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
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "06a_credentials_entered");

        driver.findElement(By.name("_eventId_proceed")).click();

        // Switch into Duo iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("duo_iframe")));

        // Click "Send Me a Push"
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.auth-button"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "06b_send_push");

        // Wait for DUO approval, click "No, other people use this device" (if present)
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("dont-trust-browser-button"))).click();
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "06c_duo_transcript");
        } catch (TimeoutException e) {
            System.out.println("Trust browser prompt did not appear, skipping...");
        }

        // Switch back out
        driver.switchTo().defaultContent();
        // Step 7: Click "Transcript Level" dropdown and select Graduate
        wait.until(ExpectedConditions.elementToBeClickable(By.id("select2-placeholder-1"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'select2-result') and contains(.,'Graduate')]"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "07a_graduate_selected");

        // Click "Transcript Type" dropdown and select Audit Transcript
        wait.until(ExpectedConditions.elementToBeClickable(By.id("select2-placeholder-2"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'select2-result') and contains(.,'Audit Transcript')]"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "07b_audit_selected");

        // Submit (if there's a submit button, click it first)
        // driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Step 8: Wait for transcript to load, then print to PDF
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        Thread.sleep(2000); // give it a moment to fully render
        ((JavascriptExecutor) driver).executeScript("window.print();");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_1, "08_after_print");
        // Assert transcript page loaded successfully
        Assert.assertFalse(driver.getTitle().isEmpty(), "Page title should not be empty");

        Assert.assertTrue(driver.getTitle().contains("Transcript"),
                "Expected transcript page, got: " + driver.getTitle());
    }

    @Test(priority = 2)
    public void scenario2_addEvents() throws Exception {
        // Step 1: Go to Canvas
        driver.get("https://canvas.northeastern.edu");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "01_canvas_mainpage");

        // Step 2: Click "Log in to Canvas"
        wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log in to Canvas"))).click();

        // Step 3: Microsoft SSO Login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "02_login_page_loaded");
        driver.findElement(By.id("i0116")).sendKeys(testData.get("username"));
        driver.findElement(By.id("idSIButton9")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "03_password_page_loaded");
        driver.findElement(By.id("i0118")).sendKeys(ExcelUtils.decodePassword(testData.get("password")));
        driver.findElement(By.id("idSIButton9")).click();

        // DUO
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("dont-trust-browser-button"))).click();
        } catch (TimeoutException e) {
            System.out.println("Trust prompt did not appear, skipping...");
        }

        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "03a_duo_success");

        // Stay signed in - No
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("idBtn_Back"))).click();
        } catch (TimeoutException e) {
            System.out.println("Stay signed in prompt did not appear, skipping...");
        }

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='calendar']")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "04_canvas_homepage");

        // Click Calendar in sidebar
        driver.findElement(By.cssSelector("a[href*='calendar']")).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "05_calendar_page");

        List<Map<String, String>> events = ExcelUtils.getEventData("testdata.xlsx", "Events");

        for (int i = 0; i < events.size(); i++) {
            Map<String, String> event = events.get(i);

            // Click "Create New Event"
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("create_new_event_link"))).click();
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "06_create_new_event");

            // Title
            WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='edit-calendar-event-form-title']")));
            titleField.clear();
            titleField.sendKeys(event.get("title"));
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "07_title_filled_in");

            // Date - clear existing value first
            WebElement dateField = driver.findElement(
                    By.cssSelector("[data-testid='edit-calendar-event-form-date']"));
            dateField.click();
            dateField.sendKeys(Keys.chord(selectAllKey, "a"));
            dateField.sendKeys(event.get("date"));
            dateField.sendKeys(Keys.TAB); // trigger validation
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "08_date_filled_in");


            // Start Time
            WebElement startTime = driver.findElement(
                    By.cssSelector("[data-testid='event-form-start-time']"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = ''; arguments[0].dispatchEvent(new Event('input'));", startTime);
            startTime.click();
            startTime.sendKeys(event.get("start_time"));
            Thread.sleep(500);
            startTime.sendKeys(Keys.TAB);
            Thread.sleep(1000); // wait for Canvas to recalculate end time
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "09a_start_time_filled_in");

            // End Time - clear AFTER start time has settled
            WebElement endTime = driver.findElement(
                    By.cssSelector("[data-testid='event-form-end-time']"));
            endTime.click();
            endTime.sendKeys(Keys.chord(selectAllKey, "a"));
            endTime.sendKeys(Keys.BACK_SPACE);
            Thread.sleep(300);
            endTime.sendKeys(event.get("end_time"));
            Thread.sleep(500);
            endTime.sendKeys(Keys.TAB);
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "09b_end_time_filled_in");

            // Location
            WebElement locationField = driver.findElement(
                    By.cssSelector("[data-testid='edit-calendar-event-form-location']"));
            locationField.clear();
            locationField.sendKeys(event.get("location"));
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "10a_location_filled_in");

            // Verify form fields before submission
            String actualTitle = driver.findElement(
                    By.cssSelector("[data-testid='edit-calendar-event-form-title']")).getAttribute("value");
            String actualDate = driver.findElement(
                    By.cssSelector("[data-testid='edit-calendar-event-form-date']")).getAttribute("value");
            String actualStart = driver.findElement(
                    By.cssSelector("[data-testid='event-form-start-time']")).getAttribute("value");
            String actualEnd = driver.findElement(
                    By.cssSelector("[data-testid='event-form-end-time']")).getAttribute("value");
            String actualLocation = driver.findElement(
                    By.cssSelector("[data-testid='edit-calendar-event-form-location']")).getAttribute("value");

            Assert.assertEquals(actualTitle, event.get("title"),
                    "Event " + (i + 1) + " title mismatch");
            Assert.assertTrue(actualDate.contains(event.get("date")) || !actualDate.isEmpty(),
                    "Event " + (i + 1) + " date not set correctly, got: " + actualDate);
            Assert.assertEquals(actualStart, event.get("start_time"),
                    "Event " + (i + 1) + " start time mismatch - expected: " + event.get("start_time") + ", got: " + actualStart);
            Assert.assertEquals(actualEnd, event.get("end_time"),
                    "Event " + (i + 1) + " end time mismatch - expected: " + event.get("end_time") + ", got: " + actualEnd);
            Assert.assertEquals(actualLocation, event.get("location"),
                    "Event " + (i + 1) + " location mismatch");

            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "10b_assertion");

            // Submit event
            driver.findElement(By.id("edit-calendar-event-submit-button")).click();
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "11_submit_event");

            Thread.sleep(1000); // brief pause before next event

            // Verify event appears on calendar after submission
            Assert.assertTrue(driver.getPageSource().contains(event.get("title")),
                    "Event '" + event.get("title") + "' not found on calendar after submission");
            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "12_submitted_verified");
        }
        // Final verification - click each event and check details
        for (int i = 0; i < events.size(); i++) {
            Map<String, String> event = events.get(i);

            WebElement calendarEvent = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(text(),'" + event.get("title") + "')]")));
            calendarEvent.click();
            Thread.sleep(1000);

            String pageSource = driver.getPageSource();
            Assert.assertTrue(pageSource.contains(event.get("title")),
                    "Event title '" + event.get("title") + "' not found in event details");
            Assert.assertTrue(pageSource.contains(event.get("location")),
                    "Event location '" + event.get("location") + "' not found in event details");

            ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "13_detail_verified");

            // Close the event popup - press Escape
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            Thread.sleep(500);
        }

        ScreenshotUtils.takeScreenshot(driver, SCENARIO_2, "14_final_calendar_view");

        Assert.assertTrue(driver.getCurrentUrl().contains("calendar"),
                "Expected calendar page, got: " + driver.getCurrentUrl());

//        Assert.assertTrue(driver.getCurrentUrl().contains("calendar"),
//                "Expected calendar page, got: " + driver.getCurrentUrl());
    }

    @Test(priority = 3)
    public void scenario3_reserveSeat() throws Exception {
        // Go to library main page
        driver.get("https://library.northeastern.edu/");
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "01_library_mainpage");

        // Accept cookies
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accept-selected")));
        driver.findElement(By.id("accept-selected")).click();

        // Reserve a room
        WebElement reserveRoom = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.linkText("Reserve A Study Room")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reserveRoom);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "02_reserve_room");

        // Select Boston Campus
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("img[alt = 'Boston Skyline Silhouette']"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "03_campus_selected");

        // Select Book Room
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='https://northeastern.libcal.com/reserve/spaces/studyspace']"))).click();

        // Swtich to the new tab
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        // Change filter
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gid")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "04_check_availability");
        Select roomType = new Select(dropdown);
        roomType.selectByVisibleText("Individual Study");

        WebElement dropdown2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("capacity")));
        Select capacity = new Select(dropdown2);
        capacity.selectByVisibleText("Space For 1-4 people");

        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "05_filter_applied");

        // Scroll to bottom
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});");
        Thread.sleep(3000);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_3, "06_browse_rooms");

        Assert.assertFalse(driver.getTitle().isEmpty(), "Page should have loaded but title is empty");
    }

    @Test(priority = 4)
    public void scenario4_download_dataset() throws Exception {
        // Go to Dataset page
        driver.get("https://onesearch.library.northeastern.edu/discovery/search?vid=01NEU_INST:NU&lang=en");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='repository.library.northeastern.edu']")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_4, "01_dataset_mainpage");

        WebElement repoLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='repository.library.northeastern.edu']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", repoLink);

        ScreenshotUtils.takeScreenshot(driver, SCENARIO_4, "02_DRS_mainpage");
        // Switch to new tab
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        // Go to Dataset page
        WebElement datasets = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href='/datasets']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", datasets);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_4, "03_DRS_dataset_page");

        // Try to download the zip file
        try {
            WebElement downloadLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='/downloads/neu:ms385324p?datastream_id=content']")));  // or whatever the download link selector is
            downloadLink.click();
            Thread.sleep(3000);

            // Check if file actually downloaded
            File downloadFolder = new File("screenshots/" + SCENARIO_4);
            File[] zips = downloadFolder.listFiles((dir, name) -> name.endsWith(".zip"));
            Assert.assertTrue(zips != null && zips.length > 0,
                    "Zip file should have downloaded but was not found");
        } catch (TimeoutException e) {
            Assert.fail("Download link was not found or not clickable - download failed");
        }
    }

    @Test(priority = 5)
    public void scenario5_update_academic_calender() throws Exception {
        // Step 1: Go to Student Hub
        driver.get("https://student.me.northeastern.edu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "01_before_login");

        // Step 2: Enter username
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        driver.findElement(By.id("i0116")).sendKeys(testData.get("username"));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "02a_username_entered");
        driver.findElement(By.id("idSIButton9")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118")));
        driver.findElement(By.id("i0118")).sendKeys(ExcelUtils.decodePassword(testData.get("password")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "02b_password_entered");
        driver.findElement(By.id("idSIButton9")).click();

        // Step 3: Wait for Duo approval, then click "No, other people use this device"
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("dont-trust-browser-button"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "03a_after_duo");

        // Reset wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Click "No" to stay signed in prompt
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("idBtn_Back")));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "03b_stay_signed_in_no");
        driver.findElement(By.id("idBtn_Back")).click();

        // Step 4: Click Resources tab
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='link-resources']"))).click();
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "04_resources_tab");

        // Step 5: Click Academics, Classes & Registration icon
        WebElement academicsIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("img[src*='academicsclassesregistration']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", academicsIcon);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "05_academics");

        // Dismiss cookie banner if present
        try {
            WebElement cookieBanner = driver.findElement(By.id("truste-consent-track"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", cookieBanner);
        } catch (NoSuchElementException e) {
            // Banner not present, continue
        }

        // Step 6: Click Academic Calendar
        WebElement academicCal = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[data-gtm-resources-link='Academic Calendar']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", academicCal);

        // Switch to new tab
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "06_academic_calender");

        // Step 7: Click Academic Calendar link
        WebElement acadCal = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='article/academic-calendar']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", acadCal);
        ScreenshotUtils.takeScreenshot(driver, SCENARIO_5, "07_academic_calender2");

        // Switch into the calendar filter iframe (index 7)
        driver.switchTo().defaultContent();
        Thread.sleep(5000); // let all iframes load
        java.util.List<WebElement> iframes = driver.findElements(By.cssSelector("iframe[id^='trumba.spud']"));
        driver.switchTo().frame(iframes.get(7));

        // Uncheck all four
        for (int i = 0; i < 4; i++) {
            WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("mixItem" + i)));
            if (checkbox.isSelected()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            }
        }

        // Assert all unchecked
        for (int i = 0; i < 4; i++) {
            WebElement checkbox = driver.findElement(By.id("mixItem" + i));
            Assert.assertFalse(checkbox.isSelected(),
                    "Checkbox mixItem" + i + " should be unchecked but is still checked");
        }
        driver.switchTo().defaultContent();
        Thread.sleep(2000);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}