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
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.quit();
  }

}

