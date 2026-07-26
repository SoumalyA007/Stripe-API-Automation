package listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentReportListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(ExtentReportListener.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        if (extent == null) {
            String reportsDir = System.getProperty("user.dir") + "/reports";
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String reportPath = reportsDir + "/ExtentReport_" + timestamp + ".html";

            // ── Archive any previous HTML reports before creating the new one ──
            archiveOldReports(reportsDir, timestamp);

            File reportFile = new File(reportPath);
            reportFile.getParentFile().mkdirs();

            ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
            htmlReporter.config().setDocumentTitle("Stripe API Automation Test Report");
            htmlReporter.config().setReportName("Stripe API Test Execution Report");
            htmlReporter.config().setTheme(Theme.DARK);
            htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");

            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Project", "Stripe API Automation");
            extent.setSystemInfo("Author", "Soumalya");
            extent.setSystemInfo("Run Timestamp", timestamp);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Test testAnnotation = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
        String[] groups = (testAnnotation != null) ? testAnnotation.groups() : new String[] {};

        ExtentTest extentTest = extent.createTest(testName);
        for (String group : groups) {
            extentTest.assignCategory(group);
        }

        test.set(extentTest);
        test.get().log(Status.INFO, "▶ Test execution started: " + testName);
        if (groups.length > 0) {
            test.get().log(Status.INFO, "Groups: " + Arrays.toString(groups));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "✔ Test passed successfully.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, "❌ Test failed.");
        if (result.getThrowable() != null) {
            test.get().fail(result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().log(Status.SKIP, "⚠ Test skipped.");
        if (result.getThrowable() != null) {
            test.get().skip(result.getThrowable());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            // Reset so the next suite run initialises a fresh report
            extent = null;
        }
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Finds all *.html files in reportsDir (excluding the one we are about to
    // create) and packs them into a single zip archive named:
    // ─────────────────────────────────────────────────────────────────────────
    private void archiveOldReports(String reportsDir, String timestamp) {
        File dir = new File(reportsDir);
        if (!dir.exists())
            return;

        File[] htmlFiles = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".html"));

        if (htmlFiles == null || htmlFiles.length == 0) {
            log.info("No previous HTML reports found to archive.");
            return;
        }

        // Create archive sub-directory
        File archiveDir = new File(reportsDir + "/archive");
        archiveDir.mkdirs();

        String zipName = archiveDir.getAbsolutePath()
                + "/ExtentReports_archive_" + timestamp + ".zip";
        File zipFile = new File(zipName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File html : htmlFiles) {
                log.info("Archiving old report: {}", html.getName());
                try (FileInputStream fis = new FileInputStream(html)) {
                    zos.putNextEntry(new ZipEntry(html.getName()));
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
                // Delete original HTML after adding it to the zip
                if (!html.delete()) {
                    log.warn("Could not delete archived report file: {}", html.getName());
                }
            }
            log.info("✅ Archived {} old report(s) → {}", htmlFiles.length, zipFile.getName());
        } catch (IOException e) {
            log.error("❌ Failed to create report archive: {}", e.getMessage());
        }
    }
}
