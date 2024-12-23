package com.vanatta.helene.supplies.database.twilio.sms;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.Test;

class SmsSenderTest {

  @Test
  void recordMessage() {
    int beforeCount = countSendHistoryRecords();
    SmsSender.recordMessage(
        TestConfiguration.jdbiTest,
        SmsSender.MessageResult.builder()
            .toNumber("123")
            .messageLength("message".length())
            .messageLink("/fake/uri.json")
            .success(false)
            .errorCode(-1)
            .errorMessage("SMS not enabled")
            .build());

    assertThat(countSendHistoryRecords()).isEqualTo(beforeCount + 1);
  }

  private static int countSendHistoryRecords() {
    String count = "select count(*) from sms_send_history";
    return TestConfiguration.jdbiTest.withHandle(
        handle -> handle.createQuery(count).mapTo(Integer.class).one());
  }
}
