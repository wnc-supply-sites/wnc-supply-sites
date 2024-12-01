package com.vanatta.helene.supplies.database.incoming.webhook;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class does two thing:
 *
 * <p>(1) cleans up and parses String value sent from Make to us, and converts the String to
 * something we can parse as a JSON.
 *
 * <p>(2) Handles validation that the input has an "auth-secret" that is valid.
 *
 * <p>Make sends JSON strings to our webhooks as an encoded string. There are lots of extra double
 * quotes and extra escaping characters that all need to be removed. This class processes those
 * strings and returns something that is a valid JSON.
 */
@Component
// @AllArgsConstructor
@Slf4j
public class IncomingJsonParser {
  private static final Gson gson = new Gson();
  private final String authSecret;
  // max length is very large here. Intended to future proof us a bit,
  // but still avoid running a lot of processing for input that
  // is clearly too long to be legit.
  private static final int INPUT_MAX_CHARACTER_LENGTH = 6400;

  public IncomingJsonParser(@Value("${webhook.auth.secret}") String authSecret) {
    this.authSecret = authSecret;
    if (authSecret == null || authSecret.isBlank()) {
      throw new IllegalArgumentException(
          "Webhook secret not specified! SET THE ENVIRONMENT VARIABLE: 'WEBHOOK_SECRET'");
    }
  }

  /**
   * Thrown when data incoming into a webhook is missing auth-secret or the auth-secret value is
   * bad.
   */
  public static class BadAuthException extends IllegalArgumentException {
    public BadAuthException(String input) {
      super(
          "No auth-secret, or invalid value found in input, input:"
              + input.substring(0, Math.min(200, input.length())));
    }
  }

  /** Converts String input from Make endpoint to a class object. */
  public <T> T parse(Class<T> jsonClass, String input) {
    if (input == null) {
      throw new IllegalArgumentException("No incoming webhook data was received.");
    }

    if (input.length() > INPUT_MAX_CHARACTER_LENGTH) {
      log.warn("Input rejected, too long! {}", input);
      throw new IllegalArgumentException("Input too long");
    }
    if (!input.contains("authSecret")) {
      throw new BadAuthException(input);
    }

    String json = cleanupString(input);

    AuthJson authJson = parseJson(json);
    if (authSecret.equals(authJson.authSecret)) {
      return gson.fromJson(json, jsonClass);
    } else {
      throw new BadAuthException(json);
    }
  }

  private static AuthJson parseJson(String input) {
    try {
      return gson.fromJson(input, AuthJson.class);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing JSON: " + input, e);
    }
  }

  /** Small class to capture the auth secret from an incoming JSON. */
  @Data
  static class AuthJson {
    String authSecret;
  }

  /**
   * Cleans up input from Make incoming requests. The strings are oddly encoded with literal newline
   * characters (ie: \n), leading & trailing double quotes, and all double quotes have a leading
   * backslash that needs to be removed. All of these values are literals in the string, they are
   * effectively double escaped and we need to remove these to have a valid JSON string.
   */
  static String cleanupString(String input) {
    if (input == null) {
      throw new NullPointerException();
    }

    return input
        // remove leading double quote
        .replaceAll("\"\\{", "{")
        // remove trailing double quote
        .replaceAll("}\"", "}")
        // remove newline characters
        .replaceAll("\\\\n", "")
        // unescape double quotes, \"  -> "
        .replaceAll("\\\\\"", "\"")
        // unescape again, double quotes will have a double escaping
        // Convert  \\"  to  \"
        .replaceAll("\\\\\"", "\"")
        // clean up whitespacing a little bit
        .replaceAll(" {2}", " ")
        .trim();
  }
}
