package com.vanatta.helene.supplies.database.dispatch;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.jdbi.v3.core.Jdbi;

public class DispatchDao {

  /**
   * @return returns next value of the dispatch number sequence
   */
  public static long nextDispatchNumber(Jdbi jdbi) {
    String query = "select nextval('dispatch_request_number_seq')";
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(Long.class).one());
  }

  /**
   * Creates a new 'dispatch_request' record (without any associated items)
   *
   * @return Returns ID of new record created
   */
  public static long createNewDispatchRequest(Jdbi jdbi, String publicId, String siteName) {
    String insert =
        """
        insert into dispatch_request(public_id, site_id)
        values(
          :publicId,
          (select id from site where name = :siteName)
        )
        """;

    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("publicId", publicId)
                .bind("siteName", siteName)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one());
  }

  public static Optional<Long> findOpenDispatch(Jdbi jdbi, String siteName) {
    String select =
        """
        select dr.id
        from dispatch_request dr
        where dr.site_id = (select id from site where name = :siteName)
          and status = 'NEW'
        """;

    return jdbi.withHandle(
        handle ->
            handle.createQuery(select).bind("siteName", siteName).mapTo(Long.class).findOne());
  }

  public static void addItemToRequest(
      Jdbi jdbi, long dispatchRequestId, String item, ItemStatus itemStatus) {

    String insert =
        """
            insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
            values(
              :dispatchRequestId,
              (select id from item where name = :itemName),
              (select id from item_status where name = :itemStatusName)
            )
            on conflict(dispatch_request_id, item_id) 
            do update set item_status_id = (select id from item_status where name = :itemStatusName)
            """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("dispatchRequestId", dispatchRequestId)
                .bind("itemName", item)
                .bind("itemStatusName", itemStatus.getText())
                .execute());
    updateDispatchRequestUpdateDate(jdbi, dispatchRequestId);
  }

  private static void updateDispatchRequestUpdateDate(Jdbi jdbi, long dispatchRequestId) {
    String update =
        "update dispatch_request set last_updated = now() where id = :dispatchRequestId";
    jdbi.withHandle(
        handle ->
            handle.createUpdate(update).bind("dispatchRequestId", dispatchRequestId).execute());
  }

  public static void deleteItemFromRequest(Jdbi jdbi, long dispatchRequestId, String item) {
    // remove the item, delete it

    String insert =
        """
        delete from dispatch_request_item
        where dispatch_request_id = :dispatchRequestId
            and item_id = (select id from item where name = :itemName)
        """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("dispatchRequestId", dispatchRequestId)
                .bind("itemName", item)
                .execute());
    updateDispatchRequestUpdateDate(jdbi, dispatchRequestId);
  }

  /**
   * Checks the priority of all item records in a given request, and updates the priority of the
   * 'dispatch_request' to match the max priority. If there are no items in the request, then the
   * request status is set to cancelled.
   */
  public static void updateRequestStatusAndPriority(Jdbi jdbi, long dispatchRequestId) {

    String query =
        """
            select
              distinct ist.name
            from dispatch_request dr
            join dispatch_request_item dri on dri.dispatch_request_id = dr.id
            join item_status ist on ist.id = dri.item_status_id
            where dr.id = :dispatchRequestId
            """;
    List<String> priorities =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(query)
                    .bind("dispatchRequestId", dispatchRequestId)
                    .mapTo(String.class)
                    .list());
    if (priorities.isEmpty()) {
      String updateStatus =
          """
          update dispatch_request set status = :cancelledStatus, last_updated = now() where id = :dispatchRequestId
          """;
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(updateStatus)
                  .bind(
                      "cancelledStatus",
                      DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText())
                  .bind("dispatchRequestId", dispatchRequestId)
                  .execute());
    } else {

      String priority =
          priorities.contains(ItemStatus.URGENTLY_NEEDED.getText())
              ? ItemStatus.URGENTLY_NEEDED.getText()
              : ItemStatus.NEEDED.getText();
      String updateStatus =
          """
          update dispatch_request set priority = :priority, last_updated = now() where id = :dispatchRequestId
          """;

      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(updateStatus)
                  .bind("priority", priority)
                  .bind("dispatchRequestId", dispatchRequestId)
                  .execute());
    }
  }

  public static DispatchRequestService.DispatchRequestJson lookupDispatchDetails(
      Jdbi jdbi, long dispatchRequestId) {

    String query =
        """
        select
          dr.public_id needRequestId,
          s.name requestingSite,
          dr.status,
          dr.priority,
          string_agg(i.name, ',') items
        from dispatch_request dr
        join site s on s.id = dr.site_id
        left join dispatch_request_item dri on dri.dispatch_request_id = dr.id
        left join item i on i.id = dri.item_id
        where dr.id = :dispatchRequestId
        group by dr.public_id, s.name, dr.status, dr.priority
        """;

    var result =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(query)
                    .bind("dispatchRequestId", dispatchRequestId)
                    .mapToBean(DispatchRequestDbRecord.class)
                    .one());

    return new DispatchRequestService.DispatchRequestJson(result);
  }

  @Data
  public static class DispatchRequestDbRecord {
    String needRequestId;
    String requestingSite;
    String status;
    String priority;

    /** Comma delimited list of items */
    String items;
  }
}
