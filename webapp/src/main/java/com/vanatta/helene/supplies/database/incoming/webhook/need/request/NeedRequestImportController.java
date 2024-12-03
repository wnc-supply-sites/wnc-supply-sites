package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.dispatch.DispatchDao;
import com.vanatta.helene.supplies.database.util.TrimUtil;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class NeedRequestImportController {

  private final Jdbi jdbi;

  /**
   * AKA 'dispatch request', this is a payload sent to us by remote system to let us know how a
   * dispatch request has been updated.
   */
  @Value
  @Builder(toBuilder = true)
  @AllArgsConstructor
  static class NeedRequestUpdate {
    Long airtableId;
    String status;
    String site;
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
      this.needRequestId = TrimUtil.trim(incoming.getNeedRequestId());
      this.site = TrimUtil.trim(incoming.getSite());
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

  /**
   * Update need request can create a new request if one does not already exists. If one exists, we
   * will update its status. If items are removed from an existing request, then we will create a
   * new request with those items.
   */
  @PostMapping("/import/update/need-request")
  ResponseEntity<String> updateNeedRequest(@RequestBody NeedRequestUpdate needRequestUpdate) {
    if (needRequestUpdate.isMissingData()) {
      log.warn("DATA IMPORT (INCOMPLETE DATA), received need request: {}", needRequestUpdate);
      return ResponseEntity.badRequest().body("Need-request update received with incomplete data");
    }
    log.info("DATA IMPORT, received need request update: {}", needRequestUpdate);
    needRequestUpdate = new NeedRequestUpdate(needRequestUpdate);

    // (1) is this an existing record? Lookup its ID
    Long existingRecordId = findDispatchRecord(needRequestUpdate).orElse(null);


    // this is a new dispatch request - create the dispatch request & add items
    if (existingRecordId == null) {
      long newDispatch = createNewDispatchRequest(jdbi, needRequestUpdate);
      needRequestUpdate
          .getSuppliesNeeded()
          .forEach(
              neededItem ->
                  DispatchDao.addItemToRequest(jdbi, newDispatch, neededItem, ItemStatus.NEEDED));
      needRequestUpdate
          .getSuppliesUrgentlyNeeded()
          .forEach(
              neededItem ->
                  DispatchDao.addItemToRequest(
                      jdbi, newDispatch, neededItem, ItemStatus.URGENTLY_NEEDED));
    } else {
      // existing request
      // (1) update dispatch_request status values

      // (2) update items
      // if the request status is pending or new

    }
    //
    //    // first update the dispatch request record (without updating any of the item records just
    // yet)
    //    if (!updateByAirtableId(needRequestUpdate)) {
    //      // if airtable id DNE, then we need to update
    //      if (!updateByPublicId(needRequestUpdate)) {}
    //    }

    return ResponseEntity.ok().build();
  }

  private static long createNewDispatchRequest(Jdbi jdbi, NeedRequestUpdate needRequestUpdate) {
    String insert =
        """
        insert into dispatch_request(airtable_id, public_id, site_id, status)
        values(
          :airtableId,
          :publicId,
          (select id from site where name = :siteName),
          :status
        )
        """;

    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("airtableId", needRequestUpdate.getAirtableId())
                .bind("publicId", needRequestUpdate.getNeedRequestId())
                .bind("siteName", needRequestUpdate.getSite())
                .bind("status", needRequestUpdate.getStatus())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one());
  }

  /*
         DispatchDao.createNewDispatchRequest(
           jdbi, dispatchNumberGenerator.apply(siteName), siteName);
       dispatchRequestId = DispatchDao.findOpenDispatch(jdbi, siteName).orElseThrow();
     DispatchDao.addItemToRequest(jdbi, dispatchRequestId, item, itemStatus);
     DispatchDao.deleteItemFromRequest(jdbi, dispatchRequestId, item);
   }

  */

  // find records by either airtable ID or public ID
  Optional<Long> findDispatchRecord(NeedRequestUpdate needRequestUpdate) {
    String query =
        """
    select id
    from dispatch_request dr
    where dr.public_id = :publicId or dr.airtable_id = :airtableId
    """;
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("publicId", needRequestUpdate.getNeedRequestId())
                .bind("airtableId", needRequestUpdate.getAirtableId())
                .mapTo(Long.class)
                .findOne());
  }

  boolean updateByAirtableId(NeedRequestUpdate needRequestUpdate) {
    String update =
        """
    update dispatch_request set
      public_id = :publicId,
      status = :status,
      last_updated = now()
    where airtable_id = :airtableId
    """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("publicId", needRequestUpdate.getNeedRequestId())
                .bind("status", needRequestUpdate.getStatus())
                .bind("airtableId", needRequestUpdate.getAirtableId())
                .execute());
    return false;
  }

  boolean updateByPublicId(NeedRequestUpdate needRequestUpdate) {

    return false;
  }

  void insert(NeedRequestUpdate needRequestUpdate) {}
}
