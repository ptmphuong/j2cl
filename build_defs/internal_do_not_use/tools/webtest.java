package tools;

/**
 *
 */

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Capabilities;
import com.google.testing.web.WebTest;
import org.openqa.selenium.net.PortProber;
import java.net.ServerSocket;
import java.util.Arrays;
import static java.util.concurrent.TimeUnit.SECONDS;

final class MyWebTest {

  public static void main(String args[]) {
    System.out.println(Arrays.toString(args));

    int port = PortProber.findFreePort();
    System.out.println(port);

    ServerSocket socket = null;
    try {
      socket = new ServerSocket(port);
    } catch (Exception e) {
      System.err.println("port: " + port + ". E: " + e);
      System.exit(1);
    }

    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    driver.get("http://localhost:" + port + "/hello_test.html");

  }

}

