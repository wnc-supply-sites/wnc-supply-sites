package com.vanatta.helene.supplies.database.dispatch;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.util.EnumUtil;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import jakarta.annotation.Nonnull;
import java.util.List;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Builder
public class SendDispatchRequest {

  @Nonnull private String createDispatchRequestUrl;
  @Nonnull private String cancelDispatchRequestUrl;
  /** Webhook URL for changing dispatch request priority. */
  @Nonnull private String updateDispatchRequestUrl;


  public void handleDispatch(String siteName, String item, @Nullable ItemStatus oldStatus, ItemStatus itemStatus) {
    if (itemStatus == ItemStatus.NEEDED || itemStatus == ItemStatus.URGENTLY_NEEDED) {
      DispatchPriority priority =
          (itemStatus == ItemStatus.NEEDED)
              ? DispatchPriority.P3_NORMAL
              : DispatchPriority.P2_URGENT;

      createDispatchRequest(siteName, item, priority);
    }
  }

  private void createDispatchRequest(String siteName, String item, DispatchPriority priority) {
    var dispatchRequest =
        DispatchRequestJson.builder()
            .needRequestId(siteName + " - " + item)
            .requestingSite(siteName)
            .items(List.of(item))
            .priority(priority.getDisplayText())
            .status(DispatchStatus.NEW.getDisplayText())
            .build();

    HttpPostSender.sendAsJson(createDispatchRequestUrl, dispatchRequest);
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  static class DispatchRequestJson {
    String needRequestId;
    String requestingSite;
    List<String> items;
    String priority;
    String status;
    String date;
    String lastModified;
    String created;
  }

  @Getter
  @AllArgsConstructor
  public enum DispatchStatus {
    NEW(""),
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    ;

    private final String displayText;

    static DispatchStatus fromDisplayText(String displayText) {
      return EnumUtil.mapText(values(), DispatchStatus::getDisplayText, displayText)
          .orElse(DispatchStatus.NEW);
    }
  }

  @Getter
  @AllArgsConstructor
  public enum DispatchPriority {
    P1_HIGH("P1 - Urgent Priority"),
    P2_URGENT("P2 - Urgent"),
    P3_NORMAL("P3 - Normal"),
    ;
    private final String displayText;

    static DispatchPriority fromDisplayText(String displayText) {
      return EnumUtil.mapText(values(), DispatchPriority::getDisplayText, displayText)
          .orElse(DispatchPriority.P2_URGENT);
    }
  }
}
