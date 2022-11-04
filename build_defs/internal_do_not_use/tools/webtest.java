package tools;

/**
 *
 */

import com.google.testing.web.WebTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.Arrays;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;

import java.time.Duration;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;

import tools.FileServerHandler;

class MyWebTest {

  public static void main(String args[]) throws IOException {
    String logmsg = "THISISMELOGGING AREYOU OKAY";
    log(logmsg);

    String testURL = "/src/test/java/com/google/j2cl/samples/helloworldlib/gen_SimplePassingTest.html";
    // add prefix if not

    // int port = PortProber.findFreePort();
    int port = 8500;

    String cwd = System.getProperty("user.dir");
    // System.out.println("Working Directory = " + cwd);
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    HttpContext context = server.createContext("/", new FileServerHandler(cwd));
    server.setExecutor(null);
    server.start();

    // InetSocketAddress address = server.getAddress();
    // System.out.println("Serving at: " + address.toString());

    // String runURL = address.toString() + testURL;
    String runURL = "http://localhost:" + port + testURL;
    System.out.println("RunURL is: " + runURL);

    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    driver.get(runURL);

    // wait for tests to finish
    new FluentWait<>((JavascriptExecutor) driver)
        .pollingEvery(Duration.ofMillis(100))
        .withTimeout(Duration.ofSeconds(5))
        .until(executor -> {
          var finished = executor.executeScript("return window.top.G_testRunner.isFinished()");
          log("test finished value: ");
          log(finished.toString());
          // value is true. if not true, should log
          return true;
        }
        );

    // get test results
    var results = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getTestResultsAsJson();");
    // var results = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getTestResults();");

    log("results type: " + results.getClass().getName());
    // log("results length: " + results.size());
    log(results.toString());

    var report = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();");
    log("report here");
    log(report.toString());

    var isSuccess = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    log("is success  here");
    log(isSuccess.toString());

    driver.quit();
    server.stop(0);

    // if !isSuccess
    throw new RuntimeException("stop to check log");

    // WebDriver driver = null;
    // try {
    //   driver = new WebTest().newWebDriverSession();
    // } catch (RuntimeException e) {
    //   // System.out.println("Expected WebDriver Exception: " + e.toString());
    // }

    // try {
    //   driver.quit();
    // } catch (RuntimeException e) {
    //   System.out.println("WebDriver quit successfully");
    // }

    // server.stop(1);

  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}

