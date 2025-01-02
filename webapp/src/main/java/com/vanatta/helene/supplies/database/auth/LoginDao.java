package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.HashingUtil;
import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public class LoginDao {

  public static void recordLoginSuccess(Jdbi jdbi, String phoneNumber) {
    String insert =
        """
        insert into login_history(phone_number, result) values
        (:phoneNumber, true);
        """;

    jdbi.withHandle(
        handle -> handle.createUpdate(insert).bind("phoneNumber", phoneNumber).execute());
  }

  public static void recordLoginFailure(Jdbi jdbi, String phoneNumber) {
    String insert =
        """
        insert into login_history(phone_number, result) values
        (:phoneNumber, false);
        """;

    jdbi.withHandle(
        handle -> handle.createUpdate(insert).bind("phoneNumber", phoneNumber).execute());
  }

  public static String generateAuthToken(Jdbi jdbi, String user) {
    final String phone = PhoneNumberUtil.removeNonNumeric(user);
    String token = UUID.randomUUID().toString();

    String insert =
        """
      insert into wss_user_auth_key(wss_user_id, token_sha256)
      values(
        (select id from wss_user where phone = :user),
        :token_sha256
      )
      """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("user", phone)
                .bind("token_sha256", HashingUtil.sha256(token))
                .execute());

    return token;
  }

  public static boolean isLoggedIn(Jdbi jdbi, String tokenValue) {
    String insert =
        """
        select 1 from wss_user_auth_key
        where token_sha256 = :token
        """;
    return jdbi.withHandle(
            handle ->
                handle
                    .createQuery(insert)
                    .bind("token", HashingUtil.sha256(tokenValue))
                    .mapTo(Integer.class)
                    .findOne())
        .isPresent();
  }

  /** Returns the universal login token */
  public static String getAuthKeyOrGenerateIt(Jdbi jdbi) {
    // select auth key
    String authKeyFetch = "select cookie_key from auth_key";
    String authKey =
        jdbi.withHandle(
            handle -> handle.createQuery(authKeyFetch).mapTo(String.class).findOne().orElse(null));

    // if auth key is null, then generate it
    if (authKey == null) {
      authKey = UUID.randomUUID().toString();

      // insert the generated auth key
      String authKeyInsert = "insert into auth_key (cookie_key) values (:cookie_key)";
      final String authKeyToInsert = authKey;
      jdbi.withHandle(
          handle ->
              handle.createUpdate(authKeyInsert).bind("cookie_key", authKeyToInsert).execute());
    }

    return authKey;
  }
}
