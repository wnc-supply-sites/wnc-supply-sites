package com.vanatta.helene.supplies.database.auth.setup.password;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.twilio.sms.SmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class SetupPasswordControllerTest {

  private static final String accessCode = "123456";
  private static final String csrf = "csrf";

  private final SetupPasswordController controller =
      new SetupPasswordController(
          SmsSender.newDisabled(TestConfiguration.jdbiTest),
          TestConfiguration.jdbiTest,
          new AccessTokenGenerator() {
            @Override
            public String generate() {
              return accessCode;
            }
          },
          () -> csrf);

  static final String number = "1111111111";
  static final String input = String.format("{\"number\":\"%s\"}", number);

  @BeforeEach
  void setup() {
    Helper.setup();
  }

  @Nested
  class RequestParsing {

    @Test
    void canParse() {
      SetupPasswordController.SendAccessCodeRequest result =
          SetupPasswordController.SendAccessCodeRequest.parse(input);
      assertThat(result.getNumber()).isEqualTo(number);
    }
  }

  @Test
  void sendAccessCode_case_notRegistered() {
    ResponseEntity<SetupPasswordController.SendAccessCodeResponse> response =
        controller.sendAccessCode(input);
    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody().getError()).isNotNull();
    assertThat(response.getBody().getCrf()).isNull();
  }

  @Test
  void sendAccessCode_case_registered() {
    assertThat(Helper.accessTokenExists(accessCode, csrf)).isFalse();
    Helper.withRegisteredNumber(number);
    int numberOfSmsSendsRow = Helper.countSendHistoryRecords();

    ResponseEntity<SetupPasswordController.SendAccessCodeResponse> response =
        controller.sendAccessCode(input);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().getError()).isNull();
    assertThat(response.getBody().getCrf()).isEqualTo(csrf);
    assertThat(Helper.countSendHistoryRecords()).isEqualTo(numberOfSmsSendsRow + 1);
    assertThat(Helper.accessTokenExists(accessCode, csrf)).isTrue();
  }
}
