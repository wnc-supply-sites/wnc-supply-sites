package com.vanatta.helene.supplies.database.auth.setup.password.send.access.code;

import com.vanatta.helene.supplies.database.util.HashingUtil;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

public class SendAccessTokenDao {

  static boolean isPhoneNumberRegistered(Jdbi jdbi, String phoneNumber) {
    String query =
        """
        select 1
        from wss_user
        where phone = :phoneNumber
        union
        select 1
        from site
        where contact_number = :phoneNumber
        """;
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("phoneNumber", phoneNumber)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
  }

  @Getter
  @Builder
  public static class InsertAccessCodeParams {
    @Nonnull String phoneNumber;
    @Nonnull String accessCode;
    @Nonnull String csrfToken;

    String getAccessCode() {
      return HashingUtil.sha256(accessCode);
    }

    String getCsrfToken() {
      return HashingUtil.sha256(csrfToken);
    }
  }

  public static void insertSmsPasscode(Jdbi jdbi, InsertAccessCodeParams params) {
    String insert =
        """

        insert into sms_passcode(wss_user_id, passcode_sha256, csrf_sha256)
        values(
          (select id from wss_user where phone = :phoneNumber),
          :passcode,
          :csrf
          )
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("phoneNumber", params.getPhoneNumber())
                .bind("passcode", params.getAccessCode())
                .bind("csrf", params.getCsrfToken())
                .execute());
  }
}
