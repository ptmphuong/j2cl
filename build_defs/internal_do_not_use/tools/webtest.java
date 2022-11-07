package tools;

/**
 *
 */

import com.google.testing.web.WebTest;

import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.logging.Logger;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.io.IOException;
import tools.FileServerHandler;
import static java.util.concurrent.TimeUnit.SECONDS;

class MyWebTest {
  public static void main(String args[]) throws IOException {
    String testURL = args[1];
    if (!testURL.startswith("/") {
      testURL = "/" + testURL;
    }
    logInfo("testURL is: " + testURL);

    // set up server
    int port = PortProber.findFreePort();
    String cwd = System.getProperty("user.dir");
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    HttpContext context = server.createContext("/", new FileServerHandler(cwd));
    // server.setExecutor(null);
    server.start();

    String runURL = "http://localhost:" + port + testURL;
    logInfo("RunURL is: " + runURL);

    // set up webdriver
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    driver.get(runURL);

    // wait for tests to finish
    new FluentWait<>((JavascriptExecutor) driver)
        .pollingEvery(Duration.ofMillis(100))
        .withTimeout(Duration.ofSeconds(5))
        .until(executor -> {
          boolean finishedSuccessfully = executor.executeScript("return window.top.G_testRunner.isFinished()");
          if (!finishedSuccessfully) {
            logErr("G_testRunner has not finished successfully");
          }
          return;
        }
        );

    // doc: https://google.github.io/closure-library/api/goog.testing.TestRunner.html
    // get test results
    String report = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();");
    logInfo(report);

    boolean allTestsPassed = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    driver.quit();
    server.stop(0);

    if (!allTestsPassed) {
      System.exit(1);
    }
  }

  private static void logInfo(String s) {
    Logger.getGlobal().info(s);
  }

  private static void logErr(String s) {
    Logger.getGlobal().error(s);
  }
}

