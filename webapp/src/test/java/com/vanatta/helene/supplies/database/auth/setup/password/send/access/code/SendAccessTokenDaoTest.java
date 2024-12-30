package com.vanatta.helene.supplies.database.auth.setup.password.send.access.code;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.auth.setup.password.SetupPasswordHelper;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendAccessTokenDaoTest {
  String number = "123___1234";

  @BeforeEach
  void cleanDb() {
    SetupPasswordHelper.setup();
  }

  @Test
  void phoneNumberIsRegistered_caseNotRegistered() {
    boolean result = SendAccessTokenDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isFalse();
  }

  @Test
  void registeredPhoneNumber() {
    SetupPasswordHelper.withRegisteredNumber(number);

    boolean result = SendAccessTokenDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, number);

    assertThat(result).isTrue();
  }

  /**
   * Phone numbers that are listed as the primary contact for a site should appear as registered.
   */
  @Test
  void phoneNumberIsRegistered_caseSiteManager() {
    String siteName = TestConfiguration.addSite();
    long siteId = TestConfiguration.getSiteId(siteName);
    ManageSiteDao.updateSiteField(
        TestConfiguration.jdbiTest, siteId, ManageSiteDao.SiteField.CONTACT_NUMBER, "1234560000");

    boolean result =
        SendAccessTokenDao.isPhoneNumberRegistered(TestConfiguration.jdbiTest, "1234560000");

    assertThat(result).isTrue();
  }

  @Test
  void insertAccessCode() {
    SetupPasswordHelper.withRegisteredNumber(number);
    String accessCode = "123";
    String csrfToken = "xyz";

    boolean tokenExists = SetupPasswordHelper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isFalse();

    SendAccessTokenDao.insertSmsPasscode(
        TestConfiguration.jdbiTest,
        SendAccessTokenDao.InsertAccessCodeParams.builder()
            .phoneNumber(number)
            .accessCode(accessCode)
            .csrfToken(csrfToken)
            .build());

    tokenExists = SetupPasswordHelper.accessTokenExists(accessCode, csrfToken);
    assertThat(tokenExists).isTrue();
  }
}
