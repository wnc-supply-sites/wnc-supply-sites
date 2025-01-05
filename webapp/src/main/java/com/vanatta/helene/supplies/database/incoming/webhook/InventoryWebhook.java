package com.vanatta.helene.supplies.database.incoming.webhook;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Webhook for receiving info about category information.
 */
@Slf4j
@Controller
public class InventoryWebhook {

  @PostMapping("/webhook/inventory/update-category")
  ResponseEntity<String> updateItemCategory(@RequestBody String input) {
    log.info("Received item category info: {}", input);
    
    return ResponseEntity.ok("ok");
  }
  

  @Value
  static class ItemTagsInput {
    long wssId;
    List<String> descriptionTags;
  }
}
