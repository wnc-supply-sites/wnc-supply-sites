package com.vanatta.helene.supplies.database.auth.setup.password.send.access.code;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.auth.setup.password.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendAccessTokenDaoTest {
  String number = "123___1234";

  @BeforeEach
  void cleanDb() {
    Helper.setup();
  }

  @Test
  void phoneNumberIsRegistered() {
    boolean result = SendAccessTokenDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isFalse();
  }

  @Test
  void registeredPhoneNumber() {
    Helper.withRegisteredNumber(number);

    boolean result = SendAccessTokenDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isTrue();
  }

  @Test
  void insertAccessCode() {
    Helper.withRegisteredNumber(number);
    String accessCode = "123";
    String csrfToken = "xyz";

    boolean tokenExists = Helper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isFalse();

    SendAccessTokenDao.insertSmsPasscode(
        TestConfiguration.jdbiTest,
        SendAccessTokenDao.InsertAccessCodeParams.builder()
            .phoneNumber(number)
            .accessCode(accessCode)
            .csrfToken(csrfToken)
            .build());

    tokenExists = Helper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isTrue();
  }
}
