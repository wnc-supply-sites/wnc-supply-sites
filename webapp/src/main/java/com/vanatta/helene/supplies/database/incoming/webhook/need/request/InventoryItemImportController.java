package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class InventoryItemImportController {

  @PostMapping("/import/update/inventory-item")
  ResponseEntity<String> updateNeedRequest(@RequestBody Map<String, Object> body) {
    log.info("Received import data: {}", body);
    return ResponseEntity.ok().build();
  }
}
