package com.vanatta.helene.supplies.database.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Slf4j
@Controller
public class DispatchUpdatesWebhook {

  @PostMapping("/webhook/needs-request-update")
  ResponseEntity<String> updateNeedsRequest(@RequestBody Map<String, String> body) {
    log.info("Webhook needs request update received data: {}", body);
    return ResponseEntity.ok("received");
  }

}
