package com.vanatta.helene.supplies.database.dispatch;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import jakarta.annotation.Nullable;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatchRequestServiceTest {

  static class Helper {
    static int countItemsInDispatchRequest(String publicId) {
      String query =
          """
        select count(*) from dispatch_request dr
        join dispatch_request_item dri on dri.dispatch_request_id = dr.id
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle ->
              handle.createQuery(query).bind("publicId", publicId).mapTo(Integer.class).one());
    }

    static String getDispatchPriority(String publicId) {
      String query =
          """
        select dr.priority from dispatch_request dr
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle -> handle.createQuery(query).bind("publicId", publicId).mapTo(String.class).one());
    }

    @Nullable
    static String getDispatchStatus(String publicId) {
      String query =
          """
        select dr.status from dispatch_request dr
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle ->
              handle
                  .createQuery(query)
                  .bind("publicId", publicId)
                  .mapTo(String.class)
                  .findOne()
                  .orElse(null));
    }
  }

  static final String SITE1_NEW_DISPATCH = "#1 site1";
  static final String SITE2_PENDING_DISPATCH = "#2 site2";
  static final String SITE3_NEW_DISPATCH = "#30 site3 new";
  static final String SITE3_PENDING_DISPATCH = "#33 site3 pending";
  static final String SITE4_NO_DISPATCH = "#4 site4";
  static final String TEST_DISPATCH = "#Test";

  DispatchRequestService dispatchRequestService =
      new DispatchRequestService(jdbiTest, _ -> TEST_DISPATCH);

  @BeforeEach
  void dbSetup() {
    TestConfiguration.setupDatabase();

    //     DB setup
    // site1: has a NEW dispatch request
    // site2: has a PENDING dispatch request
    // site3: has a NEW & PENDING dispatch request
    // site4: no dispatch requests
    List.of(
            String.format(
                """
        insert into dispatch_request(public_id, priority, status, site_id)
        values(
          '%s',
          'P1',
          'NEW',
          (select id from site where name = 'site1')
        )
        """,
                SITE1_NEW_DISPATCH),
            String.format(
                """
            insert into dispatch_request(public_id, priority, status, site_id)
            values(
              '%s',
              'P1',
              'PENDING',
              (select id from site where name = 'site2')
            )
            """,
                SITE2_PENDING_DISPATCH),
            String.format(
                """
            insert into dispatch_request(public_id, priority, status, site_id)
            values(
              '%s',
              'P1',
              'NEW',
              (select id from site where name = 'site3')
            )
            """,
                SITE3_NEW_DISPATCH),
            String.format(
                """
                insert into dispatch_request(public_id, priority, status, site_id)
                values(
                  '%s',
                  'Urgently Needed',
                  'PENDING',
                  (select id from site where name = 'site3')
                )
                """,
                SITE3_PENDING_DISPATCH))
        .forEach(sql -> jdbiTest.withHandle(handle -> handle.createUpdate(sql).execute()));

    // validate that all of the dispatch requests have no items associated with them
    List.of(
            SITE1_NEW_DISPATCH,
            SITE2_PENDING_DISPATCH,
            SITE3_NEW_DISPATCH,
            SITE3_PENDING_DISPATCH,
            SITE4_NO_DISPATCH,
            TEST_DISPATCH)
        .forEach(publicId -> assertThat(Helper.countItemsInDispatchRequest(publicId)).isEqualTo(0));
  }

  @Test
  void addItemToExistingNewDispatch() {
    dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(1);

    dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.AVAILABLE);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(0);

    var result = dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(0);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());

    result = dispatchRequestService.computeDispatch("site1", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(0);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
  }

  /** Site2 has a pending dispatch. Any dispatches we should should go to a new dispatch request. */
  @Test
  void site2_computeDispatch_withPending() {
    /* Adding a needed item should create a new dispatch */
    dispatchRequestService.computeDispatch("site2", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(Helper.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH)).isEqualTo(0);

    /* Adding another needed item should go to the new dispatch */
    dispatchRequestService.computeDispatch("site2", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(2);
    assertThat(Helper.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH)).isEqualTo(0);
  }

  /**
   * Site3 has an existing pending & new dispatch request. Adding a needed item should be added to
   * the new dispatch request, not the pending.
   */
  @Test
  void site3_computeDispatch() {
    var result = dispatchRequestService.computeDispatch("site3", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH)).isEqualTo(0);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_NEW_DISPATCH)).isEqualTo(1);
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    result = dispatchRequestService.computeDispatch("site3", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH)).isEqualTo(0);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_NEW_DISPATCH)).isEqualTo(2);
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
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(0);
    assertThat(Helper.getDispatchStatus(TEST_DISPATCH)).isNull();
    assertThat(result).isEmpty();

    // add needed water - should have 1 item in the dispatch with normal priority
    result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(Helper.getDispatchStatus(TEST_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH)).isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    // add urgent need, gloves, priority should be bumped up & gloves added to the request list
    result = dispatchRequestService.computeDispatch("site4", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(2);
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH))
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
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH)).isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    // finally remove 'water' from the request, this should remove all items and move the dispatch
    // request status to cancelled
    result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.OVERSUPPLY);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(0);
    assertThat(Helper.getDispatchStatus(TEST_DISPATCH))
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.CANCELLED.getDisplayText());
    assertThat(result.get().getNeededItems()).isEmpty();
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();
  }

  @Test
  void site4_priorities() {
    var result = dispatchRequestService.computeDispatch("site4", "water", ItemStatus.NEEDED);
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH)).isEqualTo(ItemStatus.NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P3_NORMAL.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).isEmpty();

    result = dispatchRequestService.computeDispatch("site4", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");

    /* Sending another 'needed' item should not reduce the priority */
    result = dispatchRequestService.computeDispatch("site4", "random stuff", ItemStatus.NEEDED);
    assertThat(Helper.getDispatchPriority(TEST_DISPATCH))
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getText());
    assertThat(result.get().getPriority())
        .isEqualTo(DispatchRequestService.DispatchPriority.P2_URGENT.getDisplayText());
    assertThat(result.get().getStatus())
        .isEqualTo(DispatchRequestService.DispatchStatus.NEW.getDisplayText());
    assertThat(result.get().getNeededItems()).contains("water", "random stuff");
    assertThat(result.get().getUrgentlyNeededItems()).contains("gloves");
  }
}
