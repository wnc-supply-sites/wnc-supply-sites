package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import static com.vanatta.helene.supplies.database.DbTestHelper.DispatchRequest.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.DbTestHelper;
import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.dispatch.DispatchDao;
import com.vanatta.helene.supplies.database.dispatch.DispatchRequestService;
import com.vanatta.helene.supplies.database.supplies.filters.FilterDataDao;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NeedRequestImportControllerTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
    TestConfiguration.setupDispatchRequests();
  }

  private final NeedRequestImportController controller =
      new NeedRequestImportController(TestConfiguration.jdbiTest);

  /** Add a brand new record, validate we can look up the record by the new identifiers. */
  @Test
  void dispatchNewRecord() {
    long airtableId = 100L;
    String needRequestId = "Supply#100";

    assertThat(getDispatchAirtableIdByPublicId(needRequestId)).isNull();
    assertThat(getDispatchPublicIdByAirtableId(100L)).isNull();

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(airtableId)
            .needRequestId(needRequestId)
            .site("site1")
            .suppliesNeeded(List.of("gloves"))
            .suppliesUrgentlyNeeded(List.of("water"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();

    var response = this.controller.updateNeedRequest(data);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(getDispatchAirtableIdByPublicId(needRequestId)).isEqualTo(airtableId);
    assertThat(getDispatchPublicIdByAirtableId(100L)).isEqualTo(needRequestId);

    // lookup dispatch request to confirm items are added properly
    long dispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId(needRequestId);
    var details = DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, dispatchId);
    assertThat(details.getNeededItems()).isEqualTo(List.of("gloves"));
    assertThat(details.getUrgentlyNeededItems()).isEqualTo(List.of("water"));
  }

  /**
   * Update an existing record, validate we can look up identifiers with the new data, and that the
   * status has been updated from NEW to Pending. Then, send a second update where we will update
   * the 'needs-request-id' by the 'airtable-id'.
   */
  @Test
  void dispatchUpdateExistingRecord() {
    assertThat(getDispatchAirtableIdByPublicId(TestConfiguration.SITE1_NEW_DISPATCH)).isNull();
    assertThat(getDispatchStatus(TestConfiguration.SITE1_NEW_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(getDispatchPublicIdByAirtableId(300L)).isNull();

    // update data, add airtable ID & change status
    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(300L)
            .needRequestId(TestConfiguration.SITE1_NEW_DISPATCH)
            .site("site1")
            .suppliesNeeded(List.of("gloves"))
            .suppliesUrgentlyNeeded(List.of("water"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(getDispatchAirtableIdByPublicId(TestConfiguration.SITE1_NEW_DISPATCH))
        .isEqualTo(300L);
    assertThat(getDispatchPublicIdByAirtableId(300L))
        .isEqualTo(TestConfiguration.SITE1_NEW_DISPATCH);
    assertThat(getDispatchStatus(TestConfiguration.SITE1_NEW_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());

    // send a second request, this time the airtable ID should be known & and we can update the
    // need-request-id, & update the item list

    data =
        data.toBuilder()
            .needRequestId("#-1 Updated")
            .suppliesNeeded(List.of("water", "used clothes"))
            .suppliesUrgentlyNeeded(List.of("gloves"))
            .status(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText())
            .build();
    response = controller.updateNeedRequest(data);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(getDispatchAirtableIdByPublicId("#-1 Updated")).isEqualTo(300L);
    assertThat(getDispatchPublicIdByAirtableId(300L)).isEqualTo("#-1 Updated");
    assertThat(getDispatchStatus(TestConfiguration.SITE1_NEW_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());

    // lookup dispatch request to confirm items are added properly
    long dispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#-1 Updated");
    var details = DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, dispatchId);
    assertThat(details.getNeededItems()).isEqualTo(List.of("water", "used clothes"));
    assertThat(details.getUrgentlyNeededItems()).isEqualTo(List.of("gloves"));
  }

  /**
   * Send an update that contains an item not already in the database. It should be added. There
   * should be a follow up action to send a request for item update (so that we can get the airtable
   * ID recorded)
   */
  @Test
  void dispatchUpdateWithNewItems() {
    assertThat(FilterDataDao.getAllItems(TestConfiguration.jdbiTest))
        .doesNotContain("brand-new-item-dne-needed", "brand-new-item-dne-needed-urgently");

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(200L)
            .needRequestId("Supply#200")
            .site("site1")
            .suppliesNeeded(List.of("brand-new-item-dne-needed", "water", "used clothes"))
            .suppliesUrgentlyNeeded(List.of("brand-new-item-dne-needed-urgently"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(FilterDataDao.getAllItems(TestConfiguration.jdbiTest))
        .contains("brand-new-item-dne-needed", "brand-new-item-dne-needed-urgently");

    // lookup dispatch request to confirm items are added properly
    long dispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#-1 Updated");
    var details = DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, dispatchId);
    assertThat(details.getNeededItems())
        .contains("brand-new-item-dne-needed", "water", "used clothes");
    assertThat(details.getNeededItems()).hasSize(3);
    assertThat(details.getUrgentlyNeededItems())
        .isEqualTo(List.of("brand-new-item-dne-needed-urgently"));
    assertThat(details.getUrgentlyNeededItems()).hasSize(1);
  }

  /**
   * Confirm dispatch request DNE create a dispatch request with known items.
   *
   * <p>Send an update that removes one of those items.<br>
   * Confirm the dispatch request has the item removed<br>
   * Confirm a new dispatch request exists with the removed item<br>
   */
  @Test
  void removingItemsFromAnExistingDispatchRequest_WillCreateNewDispatchRequest() {

    // clean up DB to ensure we have a clean dataset for this specific test run.
    TestConfiguration.setupDatabase();
    TestConfiguration.setupDispatchRequests();

    long airtableId = 4000L;
    String publicId = "#4000 - test";

    assertThat(getDispatchAirtableIdByPublicId(publicId)).isNull();
    assertThat(getDispatchPublicIdByAirtableId(airtableId)).isNull();

    // create the need request
    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(airtableId)
            .needRequestId(publicId)
            .site("site1")
            .suppliesNeeded(List.of("water", "used clothes"))
            .suppliesUrgentlyNeeded(List.of("gloves"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);

    // ensure request is created
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(getDispatchPublicIdByAirtableId(airtableId)).isEqualTo(publicId);

    // lookup the request that was created
    List<Long> dispatchRequestIds = DbTestHelper.DispatchRequest.getDispatchRequestsBySite("site1");
    assertThat(dispatchRequestIds).hasSize(1);
    long dispatchId = dispatchRequestIds.getFirst();

    // update the request - remove water
    // this should update the current request & create a new one with water
    data = data.toBuilder().suppliesNeeded(List.of("used clothes")).build();
    response = controller.updateNeedRequest(data);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    var firstRequest = DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, dispatchId);
    // ensure that the first request, the updated reques - is updated
    assertThat(firstRequest.getNeededItems()).contains("used clothes");
    assertThat(firstRequest.getNeededItems()).hasSize(1);
    assertThat(firstRequest.getUrgentlyNeededItems()).contains("gloves");
    assertThat(firstRequest.getUrgentlyNeededItems()).hasSize(1);

    // there should now be a second request. Let's find that & check the details
    dispatchRequestIds = DbTestHelper.DispatchRequest.getDispatchRequestsBySite("site1");
    assertThat(dispatchRequestIds).hasSize(2);
    dispatchRequestIds.remove(dispatchId);

    long newDispatchRequestId = dispatchRequestIds.getFirst();
    var newRequest =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchRequestId);
    assertThat(newRequest.getNeededItems()).contains("water");
    assertThat(newRequest.getNeededItems()).hasSize(1);
    assertThat(newRequest.getUrgentlyNeededItems()).isEmpty();
    assertThat(newRequest.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
  }

  /**
   * Remove items from a pending request creates a new request.
   *
   * <p>Create setup:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves, soap]}
   * </pre>
   *
   * <p>Remove gloves from request (1), result should be:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [soap]}
   * dispatch_request (2): {status: cancelled, items: [gloves]}
   * </pre>
   */
  @Test
  void removeItemsFromPending_ShouldCreateNew() {
    TestConfiguration.setupDatabase();

    String script =
        """
        delete from dispatch_request_item;
        delete from dispatch_request;
        delete from site_item;

        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );

        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#1000',
          1000,
          'Pending',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
    """;
    TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(1000L)
            .needRequestId("#1000")
            .site("site1")
            .suppliesUrgentlyNeeded(List.of("soap"))
            .suppliesNeeded(List.of())
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
    var pendingDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
    assertThat(pendingDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());
    assertThat(pendingDispatchDetails.getNeededItems()).isEmpty();
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("soap");
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(1);

    // find dispatch ID of the request we just created
    List<Long> dispatchRequestIds = DbTestHelper.DispatchRequest.getDispatchRequestsBySite("site1");
    assertThat(dispatchRequestIds).hasSize(2);
    dispatchRequestIds.remove(pendingDispatchId);
    long newDispatchId = dispatchRequestIds.getFirst();

    var newDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
    assertThat(newDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(newDispatchDetails.getNeededItems()).isEmpty();
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).contains("gloves");
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).hasSize(1);
  }

  /**
   * Remove items from a pending request will add to an existing new request.
   *
   * <p>Create setup:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves, soap, used clothes]}
   * dispatch_request (2): {status: new, items: [water]}
   * </pre>
   *
   * <p>Remove gloves & soap from request (1), result should be:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [used clothes]}
   * dispatch_request (2): {status: cancelled, items: [water, gloves, soap]}
   * </pre>
   */
  @Test
  void removeItemsFromPending_ToAddToNew() {
    TestConfiguration.setupDatabase();

    String script =
        """
        delete from dispatch_request_item;
        delete from dispatch_request;
        delete from site_item;

        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );

        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#1000',
          1000,
          'Pending',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#2000',
          2000,
          'NEW',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
    """;
    TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(1000L)
            .needRequestId("#1000")
            .site("site1")
            .suppliesUrgentlyNeeded(List.of())
            .suppliesNeeded(List.of("used clothes"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
    var pendingDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
    assertThat(pendingDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());
    assertThat(pendingDispatchDetails.getNeededItems()).contains("used clothes");
    assertThat(pendingDispatchDetails.getNeededItems()).hasSize(1);
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).isEmpty();

    // All items from the "new" dispatch requests were added to the pending.
    // This request should now be marked as cancelled
    long newDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#2000");
    var newDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
    assertThat(newDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(newDispatchDetails.getNeededItems()).isEmpty();
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).contains("gloves", "soap", "water");
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).hasSize(3);
  }

  /**
   * Add items into a pending request that exist in a new request.
   *
   * <p>Create setup:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves]}
   * dispatch_request (2): {status: new, items: [water, soap, used clothes]}
   * </pre>
   *
   * <p>Add all items from the 'new' request, result should be:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves, water, soap, used clothes]}
   * dispatch_request (2): {status: cancelled, items: [ ]}
   * </pre>
   */
  @Test
  void itemsAddedToPendingAreRemovedFromNewRequests() {
    TestConfiguration.setupDatabase();

    String script =
        """
        delete from dispatch_request_item;
        delete from dispatch_request;
        delete from site_item;

        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );

        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#1000',
          1000,
          'Pending',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#2000',
          2000,
          'NEW',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );
    """;
    TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(1000L)
            .needRequestId("#1000")
            .site("site1")
            .suppliesNeeded(List.of("gloves", "water", "soap"))
            .suppliesUrgentlyNeeded(List.of("used clothes"))
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
    var pendingDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
    assertThat(pendingDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());
    assertThat(pendingDispatchDetails.getNeededItems()).contains("used clothes");
    assertThat(pendingDispatchDetails.getNeededItems()).hasSize(1);
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("gloves", "water", "soap");
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(3);

    // All items from the "new" dispatch requests were added to the pending.
    // This request should now be marked as cancelled
    long newDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#2000");
    var newDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
    assertThat(newDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
    assertThat(newDispatchDetails.getNeededItems()).isEmpty();
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).isEmpty();
  }

  /**
   * Both add and remove items from a pending request, shoudl update a new request:
   *
   * <p>Create setup:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves, soap]}
   * dispatch_request (2): {status: new, items: [water, used clothes]}
   * </pre>
   *
   * <p>Add water, remove soap to request (1), result should be:
   *
   * <pre>
   * dispatch_request (1): {status: pending, items: [gloves, water]}
   * dispatch_request (2): {status: cancelled, items: [soap, used clothes]}
   * </pre>
   */
  @Test
  void bothAddAndRemoveItemsFromPendingRequest() {
    TestConfiguration.setupDatabase();

    String script =
        """
        delete from dispatch_request_item;
        delete from dispatch_request;
        delete from site_item;

        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into site_item(site_id, item_id, item_status_id)
        values(
          (select id from site where name = 'site1'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );

        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#1000',
          1000,
          'Pending',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request(public_id, airtable_id, status, site_id)
        values(
          '#2000',
          2000,
          'NEW',
          (select id from site where name = 'site1')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'gloves'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#1000'),
          (select id from item where name = 'water'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'soap'),
          (select id from item_status where name = 'Urgently Needed')
        );
        insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
        values(
          (select id from dispatch_request where public_id = '#2000'),
          (select id from item where name = 'used clothes'),
          (select id from item_status where name = 'Needed')
        );
    """;
    TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

    var data =
        NeedRequestImportController.NeedRequestUpdate.builder()
            .airtableId(1000L)
            .needRequestId("#1000")
            .site("site1")
            .suppliesUrgentlyNeeded(List.of("gloves", "water"))
            .suppliesNeeded(List.of())
            .status(DispatchRequestService.DispatchStatus.PENDING.getDisplayText())
            .build();
    var response = controller.updateNeedRequest(data);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
    var pendingDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
    assertThat(pendingDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.PENDING.getDisplayText());
    assertThat(pendingDispatchDetails.getNeededItems()).isEmpty();
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("gloves", "water");
    assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(2);

    // All items from the "new" dispatch requests were added to the pending.
    // This request should now be marked as cancelled
    long newDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#2000");
    var newDispatchDetails =
        DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
    assertThat(newDispatchDetails.getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(newDispatchDetails.getNeededItems()).contains("used clothes");
    assertThat(newDispatchDetails.getNeededItems()).hasSize(1);
    assertThat(newDispatchDetails.getUrgentlyNeededItems()).contains("soap");
    assertThat(newDispatchDetails.getNeededItems()).hasSize(1);
  }

  @Nested
  class CancelCases {
    /**
     * Cancel creating a new request
     *
     * <p>Create setup:
     *
     * <pre>
     * dispatch_request (1): {status: In Progress, items: [gloves]}
     * </pre>
     *
     * Send update:
     * <pre>
     * dispatch_request (1): {status: Cancelled, items: [gloves]}
     * </pre>
     *
     * Result should be:
     * <pre>
     * dispatch_request (1): {status: Cancelled, items: [gloves]}
     * dispatch_request (2): {status: NEW, items: [gloves]}
     * </pre>
     */
    @Test
    void cancelToCreateNewRequest() {
      TestConfiguration.setupDatabase();

      String script =
          """
          delete from dispatch_request_item;
          delete from dispatch_request;
          delete from site_item;
  
          insert into site_item(site_id, item_id, item_status_id)
          values(
            (select id from site where name = 'site1'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );
          insert into dispatch_request(public_id, airtable_id, status, site_id)
          values(
            '#1000',
            1000,
            'In Progress',
            (select id from site where name = 'site1')
          );
          insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
          values(
            (select id from dispatch_request where public_id = '#1000'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );
      """;
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

      var data =
          NeedRequestImportController.NeedRequestUpdate.builder()
              .airtableId(1000L)
              .needRequestId("#1000")
              .site("site1")
              .suppliesUrgentlyNeeded(List.of("gloves"))
              .suppliesNeeded(List.of())
              .status(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText())
              .build();
      var response = controller.updateNeedRequest(data);
      assertThat(response.getStatusCode().value()).isEqualTo(200);

      long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
      var pendingDispatchDetails =
          DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
      assertThat(pendingDispatchDetails.getStatus())
          .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
      assertThat(pendingDispatchDetails.getNeededItems()).isEmpty();
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("gloves");
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(1);

      // All items from the "new" dispatch requests were added to the pending.
      // This request should now be marked as cancelled
      long newDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#2000");
      var newDispatchDetails =
          DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
      assertThat(newDispatchDetails.getStatus())
          .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
      assertThat(newDispatchDetails.getNeededItems()).contains("used clothes");
      assertThat(newDispatchDetails.getNeededItems()).hasSize(1);
      assertThat(newDispatchDetails.getUrgentlyNeededItems()).contains("soap");
      assertThat(newDispatchDetails.getNeededItems()).hasSize(1);
    }




    /**
     * Cancel adds items to an existing new request
     *
     * <p>Create setup:
     *
     * <pre>
     * dispatch_request (1): {status: In Progress, items: [gloves]}
     * dispatch_request (1): {status: New, items: [water]}
     * </pre>
     *
     * Send update:
     * <pre>
     * dispatch_request (1): {status: Cancelled, items: [gloves]}
     * </pre>
     *
     * Result should be:
     * <pre>
     * dispatch_request (1): {status: Cancelled, items: [gloves]}
     * dispatch_request (2): {status: NEW, items: [water, gloves]}
     * </pre>
     */
    @Test
    void cancelToAddToExistingNewRequest() {
      TestConfiguration.setupDatabase();

      String script =
          """
          delete from dispatch_request_item;
          delete from dispatch_request;
          delete from site_item;
  
          insert into site_item(site_id, item_id, item_status_id)
          values(
            (select id from site where name = 'site1'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );
          insert into dispatch_request(public_id, airtable_id, status, site_id)
          values(
            '#1000',
            1000,
            'In Progress',
            (select id from site where name = 'site1')
          );
          insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
          values(
            (select id from dispatch_request where public_id = '#1000'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );


          insert into site_item(site_id, item_id, item_status_id)
          values(
            (select id from site where name = 'site1'),
            (select id from item where name = 'water'),
            (select id from item_status where name = 'Urgently Needed')
          );
          insert into dispatch_request(public_id, airtable_id, status, site_id)
          values(
            '#2000',
            2000,
            'NEW',
            (select id from site where name = 'site1')
          );
          insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
          values(
            (select id from dispatch_request where public_id = '#2000'),
            (select id from item where name = 'water'),
            (select id from item_status where name = 'Urgently Needed')
          );
      """;
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

      var data =
          NeedRequestImportController.NeedRequestUpdate.builder()
              .airtableId(1000L)
              .needRequestId("#1000")
              .site("site1")
              .suppliesUrgentlyNeeded(List.of("gloves"))
              .suppliesNeeded(List.of())
              .status(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText())
              .build();
      var response = controller.updateNeedRequest(data);
      assertThat(response.getStatusCode().value()).isEqualTo(200);

      // confirm we have update #1000 to cancelled
      long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
      var pendingDispatchDetails =
          DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
      assertThat(pendingDispatchDetails.getStatus())
          .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
      assertThat(pendingDispatchDetails.getNeededItems()).isEmpty();
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("gloves");
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(1);

      // confirm that #2000 has had gloves added to it
      long newDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#2000");
      var newDispatchDetails =
          DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, newDispatchId);
      assertThat(newDispatchDetails.getStatus())
          .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
      assertThat(newDispatchDetails.getNeededItems()).isEmpty();
      assertThat(newDispatchDetails.getUrgentlyNeededItems()).contains("gloves", "water");
      assertThat(newDispatchDetails.getUrgentlyNeededItems()).hasSize(2);
    }
  }


  @Nested
  class Completed {
    /**
     * Completed changes item status to available
     *
     * <p>Create setup:
     *
     * <pre>
     * dispatch_request (1): {status: In Progress, items: [gloves]}
     * </pre>
     *
     * Send update:
     * <pre>
     * dispatch_request (1): {status: Completed, items: [gloves]}
     * </pre>
     *
     * Result should be:
     * <pre>
     * dispatch_request (1): {status: Completed, items: [gloves]}
     * Item status of 'gloves' @ site1, changed to 'available'
     * </pre>
     */
    @Test
    void completedChangeItemStatusToAvailable() {
      TestConfiguration.setupDatabase();

      String script =
          """
          delete from dispatch_request_item;
          delete from dispatch_request;
          delete from site_item;
  
          insert into site_item(site_id, item_id, item_status_id)
          values(
            (select id from site where name = 'site1'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );
          insert into dispatch_request(public_id, airtable_id, status, site_id)
          values(
            '#1000',
            1000,
            'In Progress',
            (select id from site where name = 'site1')
          );
          insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
          values(
            (select id from dispatch_request where public_id = '#1000'),
            (select id from item where name = 'gloves'),
            (select id from item_status where name = 'Urgently Needed')
          );
      """;
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(script).execute());

      var data =
          NeedRequestImportController.NeedRequestUpdate.builder()
              .airtableId(1000L)
              .needRequestId("#1000")
              .site("site1")
              .suppliesUrgentlyNeeded(List.of("gloves"))
              .suppliesNeeded(List.of())
              .status(DispatchRequestService.DispatchStatus.COMPLETED.getDisplayText())
              .build();
      var response = controller.updateNeedRequest(data);
      assertThat(response.getStatusCode().value()).isEqualTo(200);

      // confirm we have update #1000 to completed
      long pendingDispatchId = DbTestHelper.DispatchRequest.getDispatchIdByPublicId("#1000");
      var pendingDispatchDetails =
          DispatchDao.lookupDispatchDetails(TestConfiguration.jdbiTest, pendingDispatchId);
      assertThat(pendingDispatchDetails.getStatus())
          .isEqualTo(DispatchRequestService.DispatchStatus.COMPLETED.getDisplayText());
      assertThat(pendingDispatchDetails.getNeededItems()).isEmpty();
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).contains("gloves");
      assertThat(pendingDispatchDetails.getUrgentlyNeededItems()).hasSize(1);

      // confirm item status at site1 is now 'available'
      String query = """
          select
             its.name
          from site_item si
          join item_status its on its.id = si.item_status_id
          where
              si.site_id = (select id from site where name = 'site1')
              and si.item_id = (select id from item where name = 'gloves');
      """;
      String newStatus =
          TestConfiguration.jdbiTest.withHandle(
              handle -> handle.createQuery(query).mapTo(String.class).one());
      assertThat(newStatus).isEqualTo(ItemStatus.AVAILABLE.getText());
    }
  }
}
