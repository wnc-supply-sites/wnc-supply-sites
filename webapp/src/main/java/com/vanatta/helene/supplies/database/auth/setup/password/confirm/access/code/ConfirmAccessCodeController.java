package com.vanatta.helene.supplies.database.auth.setup.password.confirm.access.code;

import com.google.gson.Gson;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Receives the challenge access code that we send to a user via SMS, validates the access code. If
 * the access code is valid, then we advance the user to password reset. For apss
 */
@Controller
@Slf4j
public class ConfirmAccessCodeController {
  private final Jdbi jdbi;
  private final Supplier<String> validationTokenGenerator;

  @Autowired
  ConfirmAccessCodeController(Jdbi jdbi) {
    this(jdbi, () -> UUID.randomUUID().toString());
  }

  ConfirmAccessCodeController(Jdbi jdbi, Supplier<String> validationTokenGenerator) {
    this.jdbi = jdbi;
    this.validationTokenGenerator = validationTokenGenerator;
  }

  @PostMapping("/confirm-access-code")
  ResponseEntity<ConfirmAccessCodeResponse> confirmAccessCode(@RequestBody String input) {
    log.info("Confirm access code: {}", input);

    String validationToken = validationTokenGenerator.get();

    ConfirmAccessCodeRequest confirmAccessCodeRequest = ConfirmAccessCodeRequest.parse(input);
    if (!confirmAccessCodeRequest.isValid()) {
      log.warn("Invalid confirm access code request: {}", input);
      throw new IllegalArgumentException("Invalid confirm access code request");
    }

    int updateCount =
        ConfirmAccessCodeDao.confirmAccessCode(jdbi, confirmAccessCodeRequest, validationToken);

    if (updateCount == 1) {
      return ResponseEntity.ok(
          ConfirmAccessCodeResponse.builder().validationToken(validationToken).build());
    } else {
      return ResponseEntity.status(401)
          .body(ConfirmAccessCodeResponse.builder().error("Invalid access token").build());
    }
  }

  @Value
  public static class ConfirmAccessCodeRequest {
    String csrf;
    String confirmCode;

    static ConfirmAccessCodeRequest parse(String json) {
      return new Gson().fromJson(json, ConfirmAccessCodeRequest.class);
    }

    boolean isValid() {
      return csrf != null //
          && !csrf.isEmpty()
          && confirmCode != null
          && confirmCode.length() == 6;
    }
  }

  @Builder
  @Value
  static class ConfirmAccessCodeResponse {
    String validationToken;
    String error;
  }
}
