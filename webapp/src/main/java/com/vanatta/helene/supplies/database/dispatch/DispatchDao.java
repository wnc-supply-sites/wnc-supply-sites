package com.vanatta.helene.supplies.database.dispatch;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import java.util.List;
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
   * @return Returns ID of new record created
   */
  public static long recordNewDispatch(
      Jdbi jdbi, long dispatchNumber, DispatchRequestService.DispatchRequestJson dispatchRequest) {

    String insert =
        """
        insert into dispatch_request(public_id, priority, item_id, site_id)
        values(
          :publicId,
          :priority,
          (select id from item where name = :itemName),
          (select id from site where name = :siteName)
        )
        """;

    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("publicId", dispatchRequest.getNeedRequestId())
                .bind("priority", dispatchRequest.getPriority())
                .bind("itemName", dispatchRequest.getItems().getFirst())
                .bind("siteName", dispatchRequest.getRequestingSite())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one());
  }

  /**
   * @return Returns ID of new dispatch send request
   */
  public static long storeSendRequest(Jdbi jdbi, long dispatchId, String sendType) {
    if (!List.of("NEW", "UPDATE_PRIORITY", "CANCEL").contains(sendType)) {
      throw new IllegalArgumentException("Invalid send type: " + sendType);
    }
    String insert =
        """
        insert into dispatch_send_queue(dispatch_request_id, send_type)
        values(:dispatchId, :sendType)
        """;
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("dispatchId", dispatchId)
                .bind("sendType", sendType)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one());
  }

  /** Marks send request with a given ID as completed. */
  public static void completeSendRequest(Jdbi jdbi, long dispatchSendId) {
    String insert =
        """
        update dispatch_send_queue set send_success_date = now()
        where id = :dispatchSendId
        """;
    jdbi.withHandle(
        handle -> handle.createUpdate(insert).bind("dispatchSendId", dispatchSendId).execute());
  }

  public static String fetchDispatchPublicId(Jdbi jdbi, long dispatchId) {
    String query =
        """
        select public_id from dispatch_request where id = :dispatchId
        """;
    return jdbi.withHandle(
        handle ->
            handle.createQuery(query).bind("dispatchId", dispatchId).mapTo(String.class).one());
  }

  // TODO: handle case of not found
  public static long lookupDispatchRequestId(Jdbi jdbi, String siteName, String itemName) {
    String query =
        """
        select id
        from dispatch_request
        where item_id = (select id from item where name = :itemName)
          and site_id = (select id from site where name = :siteName)
          and date_closed is null;
        """;
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("siteName", siteName)
                .bind("itemName", itemName)
                .mapTo(Long.class)
                .one());
  }

  // TODO: test
  // TODO: implement
  public static void changeDispatchPriority(ItemStatus latestStatus, String text) {}
}
