package com.vanatta.helene.supplies.database.delivery;

import com.google.gson.Gson;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeliveryUpdate {
    long deliveryId;
    String deliveryStatus;
    List<String> dispatcherName;
    List<String> dispatcherNumber;
    List<String> driverName;
    List<String> driverNumber;
    List<Long> dropOffSiteWssId;
    List<Long> pickupSiteWssId;
    List<Long> itemListWssIds;
    String licensePlateNumbers;
    String targetDeliveryDate;

    static DeliveryUpdate parseJson(String inputJson) {
      return new Gson().fromJson(inputJson, DeliveryUpdate.class);
    }
  }

  @PostMapping(PATH_UPDATE_DELIVERY)
  ResponseEntity<String> upsertDelivery(@RequestBody String body) {
    log.info("Delivery update endpoint received: {}", body);
    
    DeliveryUpdate deliveryUpdate = DeliveryUpdate.parseJson(body);

    
    return ResponseEntity.ok("ok");
  }
}
