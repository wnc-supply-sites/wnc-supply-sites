package com.vanatta.helene.supplies.database.auth.setup.password.confirm.access.code;

import com.vanatta.helene.supplies.database.util.HashingUtil;
import org.jdbi.v3.core.Jdbi;

public class ConfirmAccessCodeDao {

  public static int confirmAccessCode(
      Jdbi jdbi,
      ConfirmAccessCodeController.ConfirmAccessCodeRequest confirmAccessCodeRequest,
      String validationToken) {

    String update =
        """
        update sms_passcode set
          confirmed = true,
          validation_key_sha256 = :validationSha256,
          date_confirmed = now()
        where csrf_sha256 = :csrf
          and passcode_sha256 = :passcode
        """;
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("validationSha256", HashingUtil.sha256(validationToken))
                .bind("csrf", HashingUtil.sha256(confirmAccessCodeRequest.getCsrf()))
                .bind("passcode", HashingUtil.sha256(confirmAccessCodeRequest.getConfirmCode()))
                .execute());
  }
}
