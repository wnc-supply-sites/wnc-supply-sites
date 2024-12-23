package com.vanatta.helene.supplies.database.auth.setup.password;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.util.HashingUtil;

public class Helper {

  public static void setup() {
    String script =
        """
        delete from sms_passcode;
        delete from wss_user;
        """;
    TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());
  }

  public static void withRegisteredNumber(String number) {
    String insert =
        """
        insert into wss_user(id, public_id, phone) values(-1, '123', :phone)
        """;
    TestConfiguration.jdbiTest.withHandle(
        handle -> handle.createUpdate(insert).bind("phone", number).execute());
  }

  public static int countSendHistoryRecords() {
    String count = "select count(*) from sms_send_history";
    return TestConfiguration.jdbiTest.withHandle(
        handle -> handle.createQuery(count).mapTo(Integer.class).one());
  }

  public static boolean accessTokenExists(String accessCode, String csrf) {
    String query =
        """
        select 1
        from sms_passcode
        where
          wss_user_id = -1
          and passcode_sha256 = :expectedPasscode
          and confirmed = false
          and csrf_sha256 = :expectedCsrf
          and validation_key_sha256 is null
        """;

    return TestConfiguration.jdbiTest
        .withHandle(
            handle ->
                handle
                    .createQuery(query)
                    .bind("expectedPasscode", HashingUtil.sha256(accessCode))
                    .bind("expectedCsrf", HashingUtil.sha256(csrf))
                    .mapTo(Integer.class)
                    .findOne())
        .isPresent();
  }

  public static boolean accessTokenExists(String accessCode, String csrf, String validationToken) {
    String query =
        """
        select 1
        from sms_passcode
        where
          wss_user_id = -1
          and passcode_sha256 = :expectedPasscode
          and confirmed = true
          and csrf_sha256 = :expectedCsrf
          and validation_key_sha256 = :expectedValidationKey
        """;

    return TestConfiguration.jdbiTest
        .withHandle(
            handle ->
                handle
                    .createQuery(query)
                    .bind("expectedPasscode", HashingUtil.sha256(accessCode))
                    .bind("expectedCsrf", HashingUtil.sha256(csrf))
                    .bind("expectedValidationKey", HashingUtil.sha256(validationToken))
                    .mapTo(Integer.class)
                    .findOne())
        .isPresent();
  }
}
