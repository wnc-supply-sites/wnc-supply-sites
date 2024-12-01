package com.vanatta.helene.supplies.database.incoming.webhook.dispatch;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.dispatch.DispatchDao;
import com.vanatta.helene.supplies.database.dispatch.DispatchRequestService;
import com.vanatta.helene.supplies.database.incoming.webhook.WebhookSecret;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DispatchUpdatesWebhookTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  private static final String exampleJson =
      "{\n \"needsRequestId\":\"#1\",\n  \"status\":\"Pending\",\n  \"authSecret\":\"open-sesame\"  \n}";

  @Test
  void acceptJsonAndUpdateDatabase() {
    DispatchUpdatesWebhook dispatchUpdatesWebhook =
        new DispatchUpdatesWebhook(
            new WebhookSecret(TestConfiguration.jdbiTest), TestConfiguration.jdbiTest);
    var response = dispatchUpdatesWebhook.updateNeedsRequest(exampleJson);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

    var details = DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, -1);
    assertThat(details.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());
  }
}
