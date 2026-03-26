package com.automation;

import org.testng.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportListener implements ITestListener {

    private final List<String[]> results = new ArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
        results.add(new String[]{
                result.getMethod().getMethodName(),
                "Completed successfully",
                "Should complete without errors",
                "PASS"
        });
    }

    @Override
    public void onTestFailure(ITestResult result) {
        results.add(new String[]{
                result.getMethod().getMethodName(),
                result.getThrowable().getMessage(),
                "Should complete without errors",
                "FAIL"
        });
    }

    @Override
    public void onFinish(ITestContext context) {
        generateReport(context.getName());
    }

    private void generateReport(String suiteName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "test-output/TestReport_" + timestamp + ".html";

        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("<html><head><style>");
            pw.println("body { font-family: Arial; padding: 20px; }");
            pw.println("table { border-collapse: collapse; width: 100%; }");
            pw.println("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }");
            pw.println("th { background: #333; color: white; }");
            pw.println(".PASS { color: green; font-weight: bold; }");
            pw.println(".FAIL { color: red; font-weight: bold; }");
            pw.println("</style></head><body>");
            pw.println("<h1>Test Execution Report - " + suiteName + "</h1>");
            pw.println("<p>Generated: " + LocalDateTime.now() + "</p>");
            pw.println("<table>");
            pw.println("<tr><th>Test Scenario Name</th><th>Expected</th><th>Actual</th><th>Pass/Fail</th></tr>");

            for (String[] row : results) {
                pw.println("<tr>");
                pw.println("<td>" + row[0] + "</td>");
                pw.println("<td>" + row[2] + "</td>");
                pw.println("<td>" + row[1] + "</td>");
                pw.println("<td class='" + row[3] + "'>" + row[3] + "</td>");
                pw.println("</tr>");
            }

            pw.println("</table></body></html>");
            System.out.println("Report generated: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}