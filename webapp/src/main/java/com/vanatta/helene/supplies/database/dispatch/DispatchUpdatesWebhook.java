package com.vanatta.helene.supplies.database.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class DispatchUpdatesWebhook {

  @PostMapping("/webhook/needs-request-update")
  ResponseEntity<String> updateNeedsRequest(@RequestBody String body) {
    log.info("Webhook needs request update received data: {}", body);

    String needsRequestId = "";
    String status;
    String priority;
    List<String> items;


    return ResponseEntity.ok("received");
  }
}
