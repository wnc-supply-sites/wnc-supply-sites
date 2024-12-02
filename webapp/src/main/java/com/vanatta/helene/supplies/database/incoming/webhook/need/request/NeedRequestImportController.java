package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import com.vanatta.helene.supplies.database.util.TrimUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class NeedRequestImportController {

  /**
   * AKA 'dispatch request', this is a payload sent to us by remote system to let us know how a
   * dispatch request has been updated.
   */
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  static class NeedRequestUpdate {
    Long airtableId;
    String status;
    String needRequestId;
    List<String> suppliesNeeded;
    List<String> suppliesUrgentlyNeeded;

    /** Copy constructor that cleans up an incoming needs request */
    NeedRequestUpdate(NeedRequestUpdate incoming) {
      if (incoming.isMissingData()) {
        throw new IllegalStateException();
      }
      this.airtableId = incoming.getAirtableId();
      this.status = TrimUtil.trim(incoming.getStatus());
      suppliesNeeded = TrimUtil.trim(incoming.getSuppliesNeeded());
      suppliesUrgentlyNeeded = TrimUtil.trim(incoming.getSuppliesUrgentlyNeeded());
    }

    boolean isMissingData() {
      return status == null
          || status.isBlank()
          || needRequestId == null
          || needRequestId.isBlank()
          || airtableId == null;
    }
  }

  @PostMapping("/import/update/need-request")
  ResponseEntity<String> updateNeedRequest(@RequestBody NeedRequestUpdate needRequestUpdate) {
    if (needRequestUpdate.isMissingData()) {
      log.warn("Need-request update received with incomplete data: {}", needRequestUpdate);
    }
    log.info("Received needRequest update: {}", needRequestUpdate);

    return ResponseEntity.ok().build();
  }
}
