package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

public class ExtentReportListener implements ITestListener {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + "/reports/ExtentReport.html";
            File file = new File(reportPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

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
            extent.setSystemInfo("Author", "Antigravity AI");
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        // Extract groups if any
        Test testAnnotation = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
        String[] groups = (testAnnotation != null) ? testAnnotation.groups() : new String[]{};

        ExtentTest extentTest = extent.createTest(testName);

        // Assign groups/categories to the report
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
        }
    }

    public static ExtentTest getTest() {
        return test.get();
    }
}
