package tools;

/**
 *
 */

import com.google.testing.web.WebTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.PortProber;

import java.util.Arrays;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;

import tools.FileServerHandler;

class MyWebTest {

  public static void main(String args[]) throws IOException {

    String testURL = "/src/test/java/com/google/j2cl/samples/helloworldlib/gen_SimplePassingTest.html";

    String cwd = System.getProperty("user.dir");
    System.out.println("Working Directory = " + cwd);

    int port = PortProber.findFreePort();
    HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
    HttpContext context = server.createContext("/", new FileServerHandler(cwd));
    server.setExecutor(null);
    server.start();

    InetSocketAddress address = server.getAddress();
    System.out.println("Serving at: " + address.toString());

    // server.stop(1);

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
  }
}

