package com.vanatta.helene.supplies.database.supplies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Webhook for receiving info about category information.
 */
@Slf4j
public class InventoryWebhook {

  @PostMapping("/webook/inventory/update-category")
  ResponseEntity<String> updateItemCategory(@RequestBody String input) {
    log.info("Received item category info: {}", input);
    
    return ResponseEntity.ok("ok");
  }
  

}
