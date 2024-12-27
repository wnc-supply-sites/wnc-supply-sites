package com.vanatta.helene.supplies.database.delivery;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class DeliveryConfirmationController {

  /*
  let response = await fetch('https://wnc-supply-sites.com/webhook/send-confirmations',
    body: JSON.stringify({
      deliveryId: inputConfig.deliveryId,
      fromSiteWssId: inputConfig.fromSiteWssId,
      toSiteWssId: inputConfig.toSiteWssId,
      deliveryDate: inputConfig.deliveryDate,
      driverPhoneNumber: inputConfig.driverPhoneNumber,
      dispatcherPhoneNumber: inputConfig.dispatcherPhoneNumber
      })
  }
  );
  */
  @PostMapping("/webhook/send-confirmations")
  ResponseEntity<String> sendDeliveryConfirmationRequests(@RequestBody String input) {

    // store the SMS requests

    // delivery_id  | sms_sent | phone number | confirm code | confirmed

    throw new UnsupportedOperationException("TODO");
  }
}
