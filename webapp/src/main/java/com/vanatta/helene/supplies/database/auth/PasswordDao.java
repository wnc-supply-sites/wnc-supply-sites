package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.HashingUtil;
import org.jdbi.v3.core.Jdbi;

public class PasswordDao {


  /** Checks if a given plaintext password matches the hashed password stored for a given usre. */
  public static boolean confirmPassword(Jdbi jdbi, String phoneNumber, String password) {
    if (password == null || password.length() < 5) {
      return false;
    }

    String select = "select password_bcrypt from wss_user where phone = :phoneNumber";
    String passwordHash =
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(select)
                        .bind("phoneNumber", phoneNumber)
                        .mapTo(String.class)
                        .findOne())
            .orElse(null);
    if (passwordHash == null) {
      return false;
    }

    return HashingUtil.verifyBCryptHash(password, passwordHash);
  }
}
