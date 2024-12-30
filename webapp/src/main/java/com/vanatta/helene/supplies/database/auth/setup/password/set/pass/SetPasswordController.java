package com.vanatta.helene.supplies.database.auth.setup.password.set.pass;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Part of the setup password flow, the last part. Accepts the users new password and then redirects
 * the user.
 */
@Slf4j
@Controller
@AllArgsConstructor
public class SetPasswordController {

  private final Jdbi jdbi;

  @PostMapping("/set-password")
  ResponseEntity<SetPasswordResponse> setPassword(
      @RequestBody String request, HttpServletResponse response) {
    SetPasswordRequest setPasswordRequest = SetPasswordRequest.parse(request);

    if (setPasswordRequest.getPassword() == null || setPasswordRequest.getPassword().length() < 5) {
      return ResponseEntity.badRequest().body(new SetPasswordResponse("Password too short"));
    } else if (EasyPasswordList.isEasyPassword(setPasswordRequest.getPassword())) {
      return ResponseEntity.badRequest()
          .body(new SetPasswordResponse("Password is too easy to guess"));
    }

    boolean success =
        SetPasswordDao.updatePassword(
            jdbi, setPasswordRequest.getValidationToken(), setPasswordRequest.getPassword());

    if (success) {
      CookieUtil.deleteCookie(response, "auth");
      return ResponseEntity.ok(SetPasswordResponse.OK);
    } else {
      return ResponseEntity.status(401).body(new SetPasswordResponse("Failed to set password"));
    }
  }

  @Value
  static class SetPasswordRequest {
    String password;
    String validationToken;

    static SetPasswordRequest parse(String json) {
      return new Gson().fromJson(json, SetPasswordRequest.class);
    }
  }

  @Value
  static class SetPasswordResponse {
    static final SetPasswordResponse OK = new SetPasswordResponse(null);
    String error;
  }
}
