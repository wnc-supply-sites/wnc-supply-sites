package com.vanatta.helene.supplies.database.incoming.webhook;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
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
  private final WebhookSecret webhookSecret;
  private static final Gson gson = new Gson();
  // max length is very large here. Intended to future proof us a bit,
  // but still avoid running a lot of processing for input that
  // is clearly too long to be legit.
  private static final int INPUT_MAX_CHARACTER_LENGTH = 6400;

  public IncomingJsonParser(Jdbi jdbi) {
    webhookSecret = new WebhookSecret(jdbi);
  }

  /**
   * Thrown when data incoming into a webhook is missing auth-secret or the auth-secret value is
   * bad.
   */
  public static class BadAuthException extends IllegalArgumentException {
    public BadAuthException(String input) {
      super(
          "No auth-secret, or invalid value found in input, input:" + input == null
              ? null
              : input.substring(0, Math.min(200, input.length())));
    }
  }

  /** Converts String input from Make endpoint to a class object. */
  public <T> T parse(Class<T> jsonClass, String input) {
    if (input.length() > INPUT_MAX_CHARACTER_LENGTH) {
      log.warn("Input rejected, too long! {}", input);
      throw new IllegalArgumentException("Input too long");
    }
    if (!input.contains("authSecret")) {
      throw new BadAuthException(input);
    }

    String json = cleanupString(input);

    AuthJson authJson = gson.fromJson(json, AuthJson.class);
    if (authJson.authSecret != null && webhookSecret.isValid(authJson.authSecret)) {
      return gson.fromJson(json, jsonClass);
    } else {
      throw new BadAuthException(json);
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
        // clean up whitespacing a little bit
        .replaceAll(" {2}", " ")
        .trim();
  }
}
