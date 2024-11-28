package com.vanatta.helene.supplies.database.dispatch;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.util.EnumUtil;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Builder
public class SendDispatchRequest {

  private final Jdbi jdbi;
  @Nonnull private final String createDispatchRequestUrl;
  @Nonnull private final String cancelDispatchRequestUrl;

  /** Webhook URL for changing dispatch request priority. */
  @Nonnull private final String updateDispatchRequestUrl;

  public void newDispatch(String siteName, String item, ItemStatus itemStatus) {
    if (!itemStatus.isNeeded()) {
      throw new IllegalArgumentException(
          "Illegal new dispatch requested for non-needed status! " + itemStatus);
    }

    DispatchPriority priority =
        (itemStatus == ItemStatus.NEEDED) ? DispatchPriority.P3_NORMAL : DispatchPriority.P2_URGENT;

    long dispatchNumber = DispatchDao.nextDispatchNumber(jdbi);

    var dispatchRequest =
        DispatchRequestJson.builder()
            .needRequestId("#" + dispatchNumber + " - " + siteName + " - " + item)
            .requestingSite(siteName)
            .items(List.of(item))
            .priority(priority.getDisplayText())
            .build();
    long dispatchId = DispatchDao.recordNewDispatch(jdbi, dispatchNumber, dispatchRequest);
    long dispatchSendId = DispatchDao.storeSendRequest(jdbi, dispatchId, "NEW");
    HttpPostSender.sendAsJson(createDispatchRequestUrl, dispatchRequest);
    DispatchDao.completeSendRequest(jdbi, dispatchSendId);
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class DispatchRequestJson {
    String needRequestId;
    String requestingSite;

    // TODO: handle fact this is always size of one.
    List<String> items;
    String priority;
    String date;
    String lastModified;
    String created;
  }

  public void cancelDispatch(String siteName, String itemName) {
    long dispatchId = DispatchDao.lookupDispatchRequestId(jdbi, siteName, itemName);
    long dispatchSendId = DispatchDao.storeSendRequest(jdbi, dispatchId, "CANCEL");
    String dispatchPublicId = DispatchDao.fetchDispatchPublicId(jdbi, dispatchId);

    // TODO
    /*
    HttpPostSender.sendAsJson(
        cancelDispatchRequestUrl,
        CancelDispatchJson.builder().needRequestId(dispatchPublicId).build());
    DispatchDao.completeSendRequest(jdbi, dispatchSendId);

     */
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  static class CancelDispatchJson {
    String needRequestId;
  }

  public void changePriority(String siteName, String itemName, ItemStatus latestStatus) {
    long dispatchId = DispatchDao.lookupDispatchRequestId(jdbi, siteName, itemName);
    DispatchDao.changeDispatchPriority(latestStatus, latestStatus.getText());
    long dispatchSendId = DispatchDao.storeSendRequest(jdbi, dispatchId, "UPDATE_PRIORITY");
    String dispatchPublicId = DispatchDao.fetchDispatchPublicId(jdbi, dispatchId);

    // TODO
    /*
    HttpPostSender.sendAsJson(
        updateDispatchRequestUrl,
        UpdateDispatchPriorityRequestJson.builder()
            .needRequestId(dispatchPublicId)
            .itemStatus(latestStatus.getText())
            .build());
    DispatchDao.completeSendRequest(jdbi, dispatchSendId);

     */
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  static class UpdateDispatchPriorityRequestJson {
    String needRequestId;
    String itemStatus;
  }

  // TODO: NOT YET USED
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
