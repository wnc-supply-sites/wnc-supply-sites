package com.vanatta.helene.supplies.database.auth;

import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class LoginDao {

  public static void recordLoginSuccess(Jdbi jdbi, String ip) {
    String insert =
        """
        insert into login_history(ip, result) values
        (:ip, true);
        """;

    jdbi.withHandle(handle -> handle.createUpdate(insert).bind("ip", ip).execute());
  }

  public static void recordLoginFailure(Jdbi jdbi, String ip) {
    String insert =
        """
        insert into login_history(ip, result) values
        (:ip, false);
        """;

    jdbi.withHandle(handle -> handle.createUpdate(insert).bind("ip", ip).execute());
  }

  public static String getAuthKeyOrGenerateIt(Jdbi jdbi) {
    // select auth key
    String authKeyFetch = "select cookie_key from auth_key";
    String authKey =
        jdbi.withHandle(handle -> handle.createQuery(authKeyFetch).mapTo(String.class).findOne().orElse(null));

    // if auth key is null, then generate it
    if(authKey == null) {
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
