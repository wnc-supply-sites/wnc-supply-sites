package com.vanatta.helene.supplies.database.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Has a webhook for incoming requests from airtable to receive status updates for deliveries. */
@Controller
@Slf4j
public class DeliveryController {

  // also does delivery upserts
  private static final String PATH_UPDATE_DELIVERY = "/webhook/update-delivery";

  @PostMapping(PATH_UPDATE_DELIVERY)
  ResponseEntity<String> addSuppliesToDelivery(@RequestBody String body) {
    log.info("Delivery update endpoint received: {}", body);
    return ResponseEntity.ok("ok");
  }
}
