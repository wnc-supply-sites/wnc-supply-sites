package com.vanatta.helene.supplies.database.auth.setup.password.set.pass;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.*;

import com.vanatta.helene.supplies.database.auth.PasswordDao;
import com.vanatta.helene.supplies.database.auth.setup.password.SetupPasswordHelper;
import com.vanatta.helene.supplies.database.auth.setup.password.confirm.access.code.ConfirmAccessCodeController;
import com.vanatta.helene.supplies.database.auth.setup.password.confirm.access.code.ConfirmAccessCodeDao;
import com.vanatta.helene.supplies.database.auth.setup.password.send.access.code.SendAccessTokenDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetPasswordDaoTest {
  String number = "1230004223";
  String csrf = "csrf token";
  String accessCode = "333666";
  String validation = "validation auth token";
  String newPassword = "new password";

  @BeforeEach
  void cleanDb() {
    SetupPasswordHelper.setup();

    // arrange - go through the reset password flow
    // register a number & do the access code confirmation
    SetupPasswordHelper.withRegisteredNumber(number);
    SendAccessTokenDao.insertSmsPasscode(
        jdbiTest,
        SendAccessTokenDao.InsertAccessCodeParams.builder()
            .phoneNumber(number)
            .csrfToken(csrf)
            .accessCode(accessCode)
            .build());
    ConfirmAccessCodeDao.confirmAccessCode(
        jdbiTest,
        ConfirmAccessCodeController.ConfirmAccessCodeRequest.builder()
            .confirmCode(accessCode)
            .csrf(csrf)
            .build(),
        validation);
  }

  /**
   * Set-up creates a validation token that can be used to update password. Use the token, update
   * password, validate the password is now correct.
   */
  @Test
  void updatePasswordUsingValidationToken() {
    assertThat(PasswordDao.confirmPassword(jdbiTest, "1230004223", newPassword)).isFalse();
    SetPasswordDao.updatePassword(jdbiTest, validation, newPassword);
    assertThat(PasswordDao.confirmPassword(jdbiTest, "1230004223", newPassword)).isTrue();
    assertThat(PasswordDao.confirmPassword(jdbiTest, "123.000.4223", newPassword)).isTrue();
    assertThat(PasswordDao.confirmPassword(jdbiTest, "(123) 000-4223", newPassword)).isTrue();
  }

  @Test
  void hasPassword() {
    String testNumber = "3336219999";
    assertThat(PasswordDao.hasPassword(jdbiTest, testNumber)).isFalse();
    SetupPasswordHelper.withRegisteredNumber(testNumber);
    assertThat(PasswordDao.hasPassword(jdbiTest, testNumber)).isTrue();
  }
}
