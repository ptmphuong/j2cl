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

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

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
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
    log("testURL is: " + testURL);

    // set up server
    int port = PortProber.findFreePort();
    String cwd = System.getProperty("user.dir");
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    HttpContext context = server.createContext("/", new FileServerHandler(cwd));
    server.start();

    String runURL = "http://localhost:" + port + testURL;
    log("RunURL is: " + runURL);

    // set up webdriver
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    driver.get(runURL);

    // doc: https://google.github.io/closure-library/api/goog.testing.TestRunner.html
    // wait for tests to finish
    new FluentWait<>((JavascriptExecutor) driver)
        .pollingEvery(Duration.ofMillis(100))
        .withTimeout(Duration.ofSeconds(5))
        .until(executor -> {
          boolean finishedSuccessfully = (boolean) executor.executeScript("return window.top.G_testRunner.isFinished()");
          if (!finishedSuccessfully) {
            log("G_testRunner has not finished successfully");
          }
          return true;
        }
        );

    // get test results
    String report = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();").toString();
    log(report);

    boolean allTestsPassed = (boolean) ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    driver.quit();
    server.stop(0);

    if (!allTestsPassed) {
      System.exit(1);
    }
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}

