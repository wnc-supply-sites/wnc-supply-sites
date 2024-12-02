package com.vanatta.helene.supplies.database.incoming.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.incoming.webhook.dispatch.DispatchUpdatesWebhook;
import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class IncomingJsonParserTest {
  private final IncomingJsonParser incomingJsonParser = new IncomingJsonParser("open-sesame");

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void authSecretMustBeSpecified() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new IncomingJsonParser(null));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new IncomingJsonParser(""));
  }

  @Nested
  class AuthIsChecked {
    static final String veryLongInput = "a".repeat(1000);

    @Test
    void extremeLengthInputIsRejected() {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> incomingJsonParser.parse(String.class, "a".repeat(15_000)));
    }

    @ParameterizedTest
    @MethodSource
    void badAuth(String input) {
      Assertions.assertThrows(
          IncomingJsonParser.BadAuthException.class,
          () -> incomingJsonParser.parse(String.class, input));
    }

    static List<String> badAuth() {
      return List.of(
          veryLongInput,
          TestDataFile.INCORRECT_AUTH.readData(),
          TestDataFile.MISSING_AUTH.readData());
    }

    @Test
    void superLongInput() {
      Assertions.assertThrows(
          IncomingJsonParser.BadAuthException.class,
          () -> incomingJsonParser.parse(String.class, veryLongInput));
    }
  }

  @Nested
  class InputCleanupAndParsing {
    @Test
    void cleanupString() {

      // read the input from a file so that we are sure the data is exact, exactly
      // matches what the incoming data is. There are so many string escapes that we may
      // not get it correct building it up as a vanilla Java string.
      var input = TestDataFile.STATUS_CHANGE_JSON.readData();

      String output = IncomingJsonParser.cleanupString(input);

      //    "{\n \"needsRequestId\":\"#1\",\n  \"status\":\"Pending\",\n
      // \"authSecret\":\"open-sesame\"  \n}"
      String expectedOutput =
          """
        { "needsRequestId":"#1", "status":"Pending", "authSecret":"open-sesame" }""";
      assertThat(output).isEqualTo(expectedOutput);
    }

    @Test
    void cleanupStringWithQuoteInJsonValue() {
      var input = TestDataFile.DATA_CONTAINS_DOUBLE_QUOTE.readData();

      String output = IncomingJsonParser.cleanupString(input);

      //    "{\n \"needsRequestId\":\"#1\",\n  \"status\":\"Pending\",\n
      // \"authSecret\":\"open-sesame\"  \n}"
      String expectedOutput =
          """
        { "needsRequestId":"Supply#313 - z\\"Test", "status":"Pending", "authSecret":"open-sesame"}""";
      assertThat(output).isEqualTo(expectedOutput);

    }

    @Test
    void canParseToObject() {
      var input = TestDataFile.STATUS_CHANGE_JSON.readData();
      var output = incomingJsonParser.parse(DispatchUpdatesWebhook.StatusUpdateJson.class, input);

      assertThat(output.getNeedsRequestId()).isEqualTo("#1");
      assertThat(output.getStatus()).isEqualTo("Pending");
    }

    @Test
    void canParseWithQuotesInValues() {
      var input = TestDataFile.DATA_CONTAINS_DOUBLE_QUOTE.readData();
      var output = incomingJsonParser.parse(DispatchUpdatesWebhook.StatusUpdateJson.class, input);

      assertThat(output.getNeedsRequestId()).isEqualTo("Supply#313 - z\"Test");
    }
  }
}
