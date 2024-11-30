package com.vanatta.helene.supplies.database.dispatch;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.util.EnumUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
@AllArgsConstructor
@Builder
public class DispatchRequestService {
  private final Jdbi jdbi;
  private Function<String, String> dispatchNumberGenerator;

  public static DispatchRequestService create(Jdbi jdbi) {
    return DispatchRequestService.builder()
        .jdbi(jdbi)
        .dispatchNumberGenerator(
            siteName -> String.format("Supply#%s - %s", DispatchDao.nextDispatchNumber(jdbi), siteName))
        .build();
  }

  public Optional<DispatchRequestJson> removeItemFromDispatch(String siteName, String itemName) {
    // while we *are* removing the item, setting its status to 'available' will remove it
    // from a dispatch request. This is a clever way to re-use code and re-use the computeDispatch
    // method.
    return computeDispatch(siteName, itemName, ItemStatus.AVAILABLE);
  }

  /**
   * Updates any open dispatch requests with the new item, creating one if needed. If item status is
   * 'not needed', then any open dispatch requests are updated to delete that item. If this results
   * in all items being deleted, the dispatch request is deleted.
   *
   * @return Empty optional if no-op, otherwise returns updated JSON data that should be sent out
   *     for update.
   */
  public Optional<DispatchRequestJson> computeDispatch(
      String siteName, String item, ItemStatus itemStatus) {
    // is there a new dispatch request open?
    //   no =>  is this item needed? no -> no-op ; yes -> create

    Long dispatchRequestId = DispatchDao.findOpenDispatch(jdbi, siteName).orElse(null);
    if (dispatchRequestId == null) {
      if (!itemStatus.isNeeded()) {
        log.info(
            "Send dispatch no-op, item is not needed & site has no NEW dispatch record, item: {}, site: {}",
            item,
            siteName);
        // no-op, no record; and this item is not needed. Nothing to do. No request is needed.
        return Optional.empty();
      } else {
        DispatchDao.createNewDispatchRequest(
            jdbi, dispatchNumberGenerator.apply(siteName), siteName);
        dispatchRequestId = DispatchDao.findOpenDispatch(jdbi, siteName).orElseThrow();
      }
    }

    // dispatch request exists or we had a no-op
    assert dispatchRequestId != null;

    if (itemStatus.isNeeded()) {
      // if the item is needed -> add it
      DispatchDao.addItemToRequest(jdbi, dispatchRequestId, item, itemStatus);
    } else {
      // is this item no longer needed? -> remove it
      DispatchDao.deleteItemFromRequest(jdbi, dispatchRequestId, item);
    }
    DispatchDao.updateRequestStatusAndPriority(jdbi, dispatchRequestId);
    var updated = DispatchDao.lookupDispatchDetails(jdbi, dispatchRequestId);
    log.info(
        "Dispatch update computed, site: {}, item: {}, status: {}, end result: {}",
        siteName,
        item,
        itemStatus,
        updated);
    return Optional.of(updated);
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class DispatchRequestJson {
    String needRequestId;
    String requestingSite;
    String status;
    String priority;
    List<String> neededItems;
    List<String> urgentlyNeededItems;

    public DispatchRequestJson(DispatchDao.DispatchRequestDbRecord dbRecord) {
      needRequestId = dbRecord.getNeedRequestId();
      requestingSite = dbRecord.getRequestingSite();
      status = dbRecord.getStatus();
      priority =
          ItemStatus.fromTextValue(dbRecord.getPriority()) == ItemStatus.URGENTLY_NEEDED
              ? DispatchPriority.P2_URGENT.getDisplayText()
              : DispatchPriority.P3_NORMAL.getDisplayText();
      neededItems =
          dbRecord.getNeededItems() == null
              ? List.of()
              : Arrays.asList(dbRecord.getNeededItems().split(","));
      urgentlyNeededItems =
          dbRecord.getUrgentlyNeededItems() == null
              ? List.of()
              : Arrays.asList(dbRecord.getUrgentlyNeededItems().split(","));
    }
  }

  @Getter
  @AllArgsConstructor
  public enum DispatchStatus {
    NEW("NEW"),
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
