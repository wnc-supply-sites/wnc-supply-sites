package com.vanatta.helene.supplies.database.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginDaoTest {

  static class Helper {
    static long countLoginHistoryRows() {
      String query = "select count(*) from login_history";
      return TestConfiguration.jdbiTest.withHandle(
          handle -> handle.createQuery(query).mapTo(Long.class).one());
    }

    static void clearAuthKeyTable() {
      String delete = "delete from auth_key";
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createUpdate(delete).execute());
    }
  }

  @BeforeAll
  static void setup() {
    Helper.clearAuthKeyTable();
  }

  /**
   * When auth key is null, it should be generated. Once generated, we should get a fixed value
   * back.
   */
  @Test
  void authKeyGenerationAndFetch() {

    CookieAuthenticator cookieAuthenticator = new CookieAuthenticator(TestConfiguration.jdbiTest);
    var key = cookieAuthenticator.getAuthKey();
    assertThat(key).isNotNull();

    // validate that the authKe value is stable (same if we fetch it again)
    assertThat(key).isEqualTo(cookieAuthenticator.getAuthKey());

    // reload the authkey, should return the same value again
    var reloadedKey = new CookieAuthenticator(TestConfiguration.jdbiTest).getAuthKey();
    assertThat(key).isEqualTo(reloadedKey);
  }

  @Test
  void loginHistory() {
    long preCount = Helper.countLoginHistoryRows();

    LoginDao.recordLoginSuccess(TestConfiguration.jdbiTest, "199-199-199-199");

    long postCount = Helper.countLoginHistoryRows();
    assertThat(postCount).isEqualTo(preCount + 1);

    LoginDao.recordLoginFailure(TestConfiguration.jdbiTest, "199-199-199-199");

    long postFailureCount = Helper.countLoginHistoryRows();
    assertThat(postFailureCount).isEqualTo(postCount + 1);
  }
}
