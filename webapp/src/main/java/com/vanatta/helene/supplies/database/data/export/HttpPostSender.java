package com.vanatta.helene.supplies.database.data.export;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpPostSender {

  public static void sendAsJson(String url, Object toSend) {
    var client = HttpClient.newHttpClient();

    var uri = URI.create(url);

    String message = new Gson().toJson(toSend);
    var request =
        HttpRequest.newBuilder(uri)
            .POST(HttpRequest.BodyPublishers.ofString(message))
            .header("Content-type", "application/json")
            .build();

    HttpResponse<String> response = null;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        log.info("Successfully sent update");
      } else {
        log.error("Bad response received: {}, {}", response, response.body());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
