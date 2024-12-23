package com.vanatta.helene.supplies.database.auth.setup.password;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetupPasswordDaoTest {
  String number = "123___1234";

  @BeforeEach
  void cleanDb() {
    Helper.setup();
  }

  @Test
  void phoneNumberIsRegistered() {
    boolean result = SetupPasswordDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isFalse();
  }

  @Test
  void registeredPhoneNumber() {
    Helper.withRegisteredNumber(number);

    boolean result = SetupPasswordDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isTrue();
  }

  @Test
  void insertAccessCode() {
    Helper.withRegisteredNumber(number);
    String accessCode = "123";
    String csrfToken = "xyz";

    boolean tokenExists = Helper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isFalse();

    SetupPasswordDao.insertSmsPasscode(
        TestConfiguration.jdbiTest,
        SetupPasswordDao.InsertAccessCodeParams.builder()
            .phoneNumber(number)
            .accessCode(accessCode)
            .csrfToken(csrfToken)
            .build());

    tokenExists = Helper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isTrue();
  }
}
