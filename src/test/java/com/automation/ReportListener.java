package com.automation;

import org.testng.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

public class ReportListener implements ITestListener {

    private final List<String[]> results = new ArrayList<>();
    private final Map<String, List<String>> scenarioScreenshots = new LinkedHashMap<>();

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        String methodName = result.getMethod().getMethodName();
        results.add(new String[]{
                methodName,
                "Completed successfully",
                "Should complete without errors",
                "PASS",
                duration + "ms"
        });
        collectScreenshots(methodName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        String methodName = result.getMethod().getMethodName();
        results.add(new String[]{
                methodName,
                result.getThrowable().getMessage(),
                "Should complete without errors",
                "FAIL",
                duration + "ms"
        });
        collectScreenshots(methodName);
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("=== onFinish called, generating report ===");
        System.out.println("Results collected: " + results.size());
        generateReport(context.getName());
    }

    private void collectScreenshots(String methodName) {
        String folderName = getScenarioFolder(methodName);
        String basePath = System.getProperty("user.dir");
        File folder = new File(basePath + "/screenshots/" + folderName);
        List<String> screenshots = new ArrayList<>();

        // Debug
//        System.out.println("=== SCREENSHOT DEBUG ===");
//        System.out.println("Method: " + methodName);
//        System.out.println("Folder: " + folderName);
//        System.out.println("Full path: " + folder.getAbsolutePath());
//        System.out.println("Exists: " + folder.exists());

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) ->
                    name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
            System.out.println("Screenshots found: " + (files != null ? files.length : 0));
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File file : files) {
                    System.out.println("  Adding: " + file.getName());
                    screenshots.add(file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("Folder does NOT exist!");
        }

        System.out.println("=== END DEBUG ===");
        scenarioScreenshots.put(methodName, screenshots);
    }

    private String getScenarioFolder(String methodName) {
        // Map your method names to folder names
        // Update these to match your actual SCENARIO constants
        if (methodName.contains("scenario1")) return "Scenario1_Download_Transcript";
        if (methodName.contains("scenario2")) return "Scenario2_Add_Event";
        if (methodName.contains("scenario3")) return "Scenario3_Reserve_Seat";
        if (methodName.contains("scenario4")) return "Scenario4_Download_Dataset";
        if (methodName.contains("scenario5")) return "Scenario5_Update_Academic_Calender";
        return methodName;
    }

    private String imageToBase64(String imagePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(imagePath));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    private void generateReport(String suiteName) {
        new File("test-output").mkdirs();

        File testOutput = new File("test-output");
        File[] reports = testOutput.listFiles((dir, name) -> name.startsWith("TestReport_") && name.endsWith(".html"));
        if (reports != null && reports.length >= 5) {
            Arrays.sort(reports, Comparator.comparingLong(File::lastModified));
            for (int i = 0; i <= reports.length - 5; i++) {
                if (!reports[i].delete()) {
                    System.out.println("Failed to delete: " + reports[i].getName());
                }
            }
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "test-output/TestReport_" + timestamp + ".html";

        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html><head>");
            pw.println("<meta charset='UTF-8'>");
            pw.println("<title>Test Report - " + suiteName + "</title>");
            pw.println("<style>");
            pw.println("body { font-family: Georgia, 'Times New Roman', serif; color: #111; padding: 40px 60px; max-width: 1200px; margin: 0 auto; }");
            pw.println("h1 { font-size: 1.8em; margin-bottom: 4px; }");
            pw.println("h2 { font-size: 1.3em; margin-bottom: 10px; }");
            pw.println(".subtitle { color: #555; font-size: 0.9em; margin-bottom: 30px; }");
            pw.println(".summary { display: flex; gap: 30px; margin-bottom: 30px; font-size: 0.95em; }");
            pw.println(".summary span { font-weight: bold; }");
            pw.println(".tabs { display: flex; gap: 2px; border-bottom: 2px solid #111; margin-bottom: 0; }");
            pw.println(".tab { padding: 10px 20px; cursor: pointer; font-size: 0.9em; border: 1px solid #ccc; border-bottom: none; border-radius: 4px 4px 0 0; background: #f5f5f5; }");
            pw.println(".tab:hover { background: #e8e8e8; }");
            pw.println(".tab.active { background: #fff; border-color: #111; font-weight: bold; position: relative; bottom: -2px; border-bottom: 2px solid #fff; }");
            pw.println(".tab-content { display: none; border: 1px solid #ccc; border-top: none; padding: 25px; }");
            pw.println(".tab-content.active { display: block; }");
            pw.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            pw.println("th { background: #f0f0f0; text-align: left; padding: 10px 12px; font-size: 0.85em; border: 1px solid #ccc; }");
            pw.println("td { padding: 10px 12px; border: 1px solid #ccc; font-size: 0.9em; }");
            pw.println(".pass { color: #1a7f37; font-weight: bold; }");
            pw.println(".fail { color: #cf222e; font-weight: bold; }");
            pw.println(".duration { color: #555; font-size: 0.85em; }");
            pw.println(".screenshots-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 16px; margin-top: 15px; }");
            pw.println(".screenshot-card { border: 1px solid #ccc; border-radius: 4px; overflow: hidden; }");
            pw.println(".screenshot-card img { width: 100%; display: block; cursor: pointer; }");
            pw.println(".screenshot-card .caption { padding: 8px 10px; font-size: 0.8em; color: #555; background: #f9f9f9; }");
            pw.println(".no-screenshots { color: #888; font-style: italic; padding: 30px; text-align: center; }");
            pw.println(".modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.85); z-index: 1000; justify-content: center; align-items: center; cursor: pointer; }");
            pw.println(".modal.active { display: flex; }");
            pw.println(".modal img { max-width: 90%; max-height: 90%; }");
            pw.println("</style></head><body>");

            // Header
            pw.println("<h1>Test Execution Report</h1>");
            pw.println("<p class='subtitle'>" + suiteName + " &mdash; " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")) + "</p>");

            // Summary
            long totalPass = results.stream().filter(r -> r[3].equals("PASS")).count();
            long totalFail = results.stream().filter(r -> r[3].equals("FAIL")).count();
            long totalDuration = results.stream().mapToLong(r -> Long.parseLong(r[4].replace("ms", ""))).sum();

            pw.println("<div class='summary'>");
            pw.println("<div>Total: <span>" + results.size() + "</span></div>");
            pw.println("<div>Passed: <span class='pass'>" + totalPass + "</span></div>");
            pw.println("<div>Failed: <span class='fail'>" + totalFail + "</span></div>");
            pw.println("<div>Duration: <span>" + String.format("%.1f", totalDuration / 1000.0) + "s</span></div>");
            pw.println("</div>");

            // Tabs
            pw.println("<div class='tabs'>");
            pw.println("<div class='tab active' onclick='showTab(\"overview\")'>Overview</div>");
            for (int i = 0; i < results.size(); i++) {
                String methodName = results.get(i)[0];
                pw.println("<div class='tab' onclick='showTab(\"scenario" + i + "\")'>" + methodName + "</div>");
            }
            pw.println("</div>");

            // Overview tab
            pw.println("<div id='overview' class='tab-content active'>");
            pw.println("<table>");
            pw.println("<tr><th>Test Scenario</th><th>Expected</th><th>Actual</th><th>Status</th><th>Duration</th></tr>");
            for (String[] row : results) {
                pw.println("<tr>");
                pw.println("<td>" + row[0] + "</td>");
                pw.println("<td>" + row[2] + "</td>");
                pw.println("<td style='max-width:400px;word-wrap:break-word;'>" + row[1] + "</td>");
                pw.println("<td class='" + row[3].toLowerCase() + "'>" + row[3] + "</td>");
                pw.println("<td class='duration'>" + row[4] + "</td>");
                pw.println("</tr>");
            }
            pw.println("</table></div>");

            // Scenario tabs
            int scenarioIndex = 0;
            for (String[] row : results) {
                String methodName = row[0];
                List<String> screenshots = scenarioScreenshots.getOrDefault(methodName, new ArrayList<>());

                pw.println("<div id='scenario" + scenarioIndex + "' class='tab-content'>");
                pw.println("<h2>" + methodName + " &mdash; <span class='" + row[3].toLowerCase() + "'>" + row[3] + "</span> (" + row[4] + ")</h2>");

                pw.println("<table>");
                pw.println("<tr><th>Field</th><th>Value</th></tr>");
                pw.println("<tr><td>Expected</td><td>" + row[2] + "</td></tr>");
                pw.println("<tr><td>Actual</td><td style='max-width:400px;word-wrap:break-word;'>" + row[1] + "</td></tr>");
                pw.println("<tr><td>Duration</td><td>" + row[4] + "</td></tr>");
                pw.println("<tr><td>Screenshots</td><td>" + screenshots.size() + " captured</td></tr>");
                pw.println("</table>");

                if (screenshots.isEmpty()) {
                    pw.println("<div class='no-screenshots'>No screenshots captured for this scenario</div>");
                } else {
                    String folderName = getScenarioFolder(methodName);
                    pw.println("<div class='screenshots-grid'>");
                    for (String path : screenshots) {
                        String fileName2 = new File(path).getName();
                        pw.println("<div class='screenshot-card'>");
                        pw.println("<img src='../screenshots/" + folderName + "/" + fileName2 + "' onclick='openModal(this.src)' />");
                        pw.println("<div class='caption'>" + fileName2 + "</div>");
                        pw.println("</div>");
                    }
                    pw.println("</div>");
                }
                pw.println("</div>");
                scenarioIndex++;
            }

            // Modal
            pw.println("<div class='modal' id='imageModal' onclick='closeModal()'>");
            pw.println("<img id='modalImage' src='' />");
            pw.println("</div>");

            // JavaScript
            pw.println("<script>");
            pw.println("function showTab(id) {");
            pw.println("  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));");
            pw.println("  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));");
            pw.println("  document.getElementById(id).classList.add('active');");
            pw.println("  event.target.classList.add('active');");
            pw.println("}");
            pw.println("function openModal(src) {");
            pw.println("  document.getElementById('modalImage').src = src;");
            pw.println("  document.getElementById('imageModal').classList.add('active');");
            pw.println("}");
            pw.println("function closeModal() {");
            pw.println("  document.getElementById('imageModal').classList.remove('active');");
            pw.println("}");
            pw.println("</script>");

            pw.println("</body></html>");
            System.out.println("Report generated: " + fileName);

            // Auto-open in browser
            try {
                java.awt.Desktop.getDesktop().browse(new File(fileName).toURI());
            } catch (Exception e) {
                System.out.println("Could not open report automatically: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}