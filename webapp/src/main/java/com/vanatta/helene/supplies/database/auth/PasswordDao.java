package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.HashingUtil;
import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import org.jdbi.v3.core.Jdbi;

public class PasswordDao {

  /** Checks if a given plaintext password matches the hashed password stored for a given usre. */
  public static boolean confirmPassword(Jdbi jdbi, String phoneNumber, String password) {
    if (phoneNumber == null || password == null || password.length() < 5) {
      return false;
    }
    final String cleanedPhoneNumber = PhoneNumberUtil.removeNonNumeric(phoneNumber);
    if (cleanedPhoneNumber.length() != 10 && cleanedPhoneNumber.length() != 11) {
      return false;
    }

    String select = "select password_bcrypt from wss_user where phone = :phoneNumber";
    String passwordHash =
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(select)
                        .bind("phoneNumber", cleanedPhoneNumber)
                        .mapTo(String.class)
                        .findOne())
            .orElse(null);
    if (passwordHash == null) {
      return false;
    }

    return HashingUtil.verifyBCryptHash(password, passwordHash);
  }
}
