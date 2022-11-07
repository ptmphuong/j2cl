package tools;

import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;

public final class FileServerHandler implements HttpHandler {
  private String rootDir;
  private String mime = "text/html";

  public FileServerHandler(String rootDir) {
    this.rootDir = rootDir;
  }

  public void handle(HttpExchange exchange) throws IOException {
      Path path = Paths.get(
          rootDir,
          // URLDecoder.decode(exchange.getRequestURI().toString(), "UTF-8")
          URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8)
      ).toAbsolutePath();

      if (path != null) {
          exchange.setAttribute("request-path", path.toString());  // store for OutputFilter
          if (!Files.exists(path) || !Files.isReadable(path)) {
            handleNotFound(exchange);
          } else {
            handleServeFile(exchange, path);
          }
      } else {
          exchange.setAttribute("request-path", "could not resolve request URI path");
          handleNotFound(exchange);
      }
  }

  private void handleServeFile(HttpExchange exchange, Path path) throws IOException {
     var respHdrs = exchange.getResponseHeaders();
     respHdrs.set("Content-Type", mime);
     exchange.sendResponseHeaders(200, Files.size(path));
     try (InputStream fis = Files.newInputStream(path);
          OutputStream os = exchange.getResponseBody()) {
         fis.transferTo(os);
     }
  }

    private void handleNotFound(HttpExchange exchange) throws IOException {
        var bytes = ("<p>Cannot find generated html file at: " +  exchange.getRequestURI().getPath() + "</p>\n").getBytes(UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
            exchange.sendResponseHeaders(404, -1);
        } else {
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }


}
