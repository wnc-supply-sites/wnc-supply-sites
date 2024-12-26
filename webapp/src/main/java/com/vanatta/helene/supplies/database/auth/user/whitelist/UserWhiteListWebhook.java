package com.vanatta.helene.supplies.database.auth.user.whitelist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Webhook that receives JSON payloads that adds users to the registration white list. Only users on
 * this white list can register & create a password. Without being on the white list, we will not
 * send them a SMS code to register.
 */
@Controller
@Slf4j
public class UserWhiteListWebhook {

  @PostMapping("/webhook/whitelist-user")
  ResponseEntity<String> whiteListUser(@RequestBody String input) {
    log.info("white list user request received: {}", input);
    return ResponseEntity.ok("ok");
  }
}
