package com.vanatta.helene.supplies.database.dispatch;

import static com.vanatta.helene.supplies.database.TestConfiguration.SITE1_NEW_DISPATCH;
import static com.vanatta.helene.supplies.database.TestConfiguration.SITE2_PENDING_DISPATCH;
import static com.vanatta.helene.supplies.database.TestConfiguration.SITE3_NEW_DISPATCH;
import static com.vanatta.helene.supplies.database.TestConfiguration.SITE3_PENDING_DISPATCH;
import static com.vanatta.helene.supplies.database.TestConfiguration.SITE4_NO_DISPATCH;
import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.DbTestHelper;
import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatchRequestServiceTest {

  // when creating a new dispatch request, this is the public ID value that will be used.
  static final String TEST_DISPATCH = "#Test";
  DispatchRequestService dispatchRequestService =
      new DispatchRequestService(jdbiTest, _ -> TEST_DISPATCH);

  @BeforeEach
  void dbSetup() {
    TestConfiguration.setupDatabase();
    TestConfiguration.setupDispatchRequests();

    // validate that all of the dispatch requests have no items associated with them
    List.of(
            SITE1_NEW_DISPATCH,
            SITE2_PENDING_DISPATCH,
            SITE3_NEW_DISPATCH,
            SITE3_PENDING_DISPATCH,
            SITE4_NO_DISPATCH,
            TEST_DISPATCH)
        .forEach(
            publicId ->
                assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(publicId))
                    .isEqualTo(0));
  }

  @Test
  void addItemToExistingNewDispatch() {
    dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE1_NEW_DISPATCH))
        .isEqualTo(1);

    dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.AVAILABLE);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE1_NEW_DISPATCH))
        .isEqualTo(0);

    var result = dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE1_NEW_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());

    result = dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE1_NEW_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
  }

  /** Site2 has a pending dispatch. Any dispatches we should should go to a new dispatch request. */
  @Test
  void site2_computeDispatch_withPending() {
    /* Adding a needed item should create a new dispatch */
    dispatchRequestService.computeDispatch("site2", "gloves", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(1);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH))
        .isEqualTo(0);

    /* Adding another needed item should go to the new dispatch */
    dispatchRequestService.computeDispatch("site2", "water", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(2);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH))
        .isEqualTo(0);
  }

  /**
   * Site3 has an existing pending & new dispatch request. Adding a needed item should be added to
   * the new dispatch request, not the pending.
   */
  @Test
  void site3_computeDispatch() {
    var result = dispatchRequestService.computeDispatch("site3", "water", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE3_NEW_DISPATCH))
        .isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    result = dispatchRequestService.computeDispatch("site3", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(SITE3_NEW_DISPATCH))
        .isEqualTo(2);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");
  }

  /**
   * Site4 has no dispatch requests, adding a needed item should create a brand new dispatch
   * request. Then, we remove items by adding them in with available item status.
   */
  @Test
  void site4_brandNewDispatch_and_RemoveItems() {
    // Adding 'available' is a no-op
    var result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.AVAILABLE);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.getDispatchStatus(TEST_DISPATCH)).isNull();
    assertThat(result).isEmpty();

    // add needed water - should have 1 item in the dispatch with normal priority
    result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(1);
    assertThat(DbTestHelper.DispatchRequest.getDispatchStatus(TEST_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    // add urgent need, gloves, priority should be bumped up & gloves added to the request list
    result = dispatchRequestService.computeDispatch("site4", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(2);
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");

    // mark gloves as available, this should remove them from the request list
    // the priority of the request list should drop back down to normal
    result = dispatchRequestService.computeDispatch("site4", "gloves", ItemStatus.AVAILABLE);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(1);
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    // finally remove 'water' from the request, this should remove all items and move the dispatch
    // request status to cancelled
    result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.OVERSUPPLY);
    assertThat(DbTestHelper.DispatchRequest.countItemsInDispatchRequest(TEST_DISPATCH))
        .isEqualTo(0);
    assertThat(DbTestHelper.DispatchRequest.getDispatchStatus(TEST_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
    assertThat(result.get().getNeededItems()).isEmpty();
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();
  }

  @Test
  void site4_priorities() {
    var result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    result = dispatchRequestService.computeDispatch("site4", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");

    /* Sending another 'needed' item should not reduce the priority */
    result = dispatchRequestService.computeDispatch("site4", "random stuff", ItemStatus.NEEDED);
    assertThat(DbTestHelper.DispatchRequest.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water", "random stuff");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");
  }
}
