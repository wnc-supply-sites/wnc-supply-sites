package com.vanatta.helene.supplies.database.incoming.webhook.dispatch;

import com.vanatta.helene.supplies.database.incoming.webhook.IncomingJsonParser;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class DispatchUpdatesWebhook {

  private final Jdbi jdbi;
  private final IncomingJsonParser incomingJsonParser;


  @PostMapping("/webhook/needs-request-update")
  ResponseEntity<String> updateNeedsRequest(@RequestBody String body) {
    log.info("Webhook, received data: {}", body);

    StatusUpdateJson incoming = incomingJsonParser.parse(StatusUpdateJson.class, body);
    if (incoming.needsRequestId == null || incoming.status == null) {
      log.warn("Invalid request received, empty data!! Data: {}", body);
      return ResponseEntity.badRequest().body("missing data");
    }

    long updateCount = updateDispatchRequest(incoming);

    if (updateCount == 1) {
      return ResponseEntity.ok("Received, success!");
    } else {
      return ResponseEntity.badRequest()
          .body("Error, dispatch request not updated. Records updated: " + updateCount);
    }
  }

  @Value
  public static class StatusUpdateJson {
    String needsRequestId;
    String status;
  }

  private int updateDispatchRequest(StatusUpdateJson incoming) {
    String update = "update dispatch_request set status = :status where public_id = :publicId";

    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("status", incoming.status)
                .bind("publicId", incoming.needsRequestId)
                .execute());
  }
}
