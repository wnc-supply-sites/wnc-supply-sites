package com.vanatta.helene.supplies.database.dispatch;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
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
  }

  static final String SITE1_NEW_DISPATCH = "#1 site1";
  static final String SITE2_PENDING_DISPATCH = "#2 site2";
  static final String SITE3_NEW_DISPATCH = "#30 site3 new";
  static final String SITE3_PENDING_DISPATCH = "#33 site3 pending";
  static final String SITE4_NO_DISPATCH = "#4 site4";
  static final String TEST_DISPATCH = "#Test";

  DispatchRequestService dispatchRequestService =
      DispatchRequestService.builder()
          .httpPost((s, o) -> {})
          .createDispatchRequestUrl("")
          .updateDispatchRequestUrl("")
          .cancelDispatchRequestUrl("")
          .dispatchNumberGenerator(() -> TEST_DISPATCH)
          .jdbi(jdbiTest)
          .build();

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
                  'P1',
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
    dispatchRequestService.addDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(1);

    dispatchRequestService.addDispatch("site1", "gloves", ItemStatus.AVAILABLE);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(0);

    dispatchRequestService.addDispatch("site1", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(1);

    dispatchRequestService.addDispatch("site1", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE1_NEW_DISPATCH)).isEqualTo(1);
  }

  /** Site2 has a pending dispatch. Any dispatches we should should go to a new dispatch request. */
  @Test
  void site2_addDispatch_withPending() {
    /* Adding a non-needed item is a no-op */
    dispatchRequestService.addDispatch("site2", "gloves", ItemStatus.AVAILABLE);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(0);
    assertThat(Helper.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH)).isEqualTo(0);

    /* Adding a needed item should create a new dispatch */
    dispatchRequestService.addDispatch("site2", "gloves", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    assertThat(Helper.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH)).isEqualTo(0);

    /* Adding another needed item should go to the new dispatch */
    dispatchRequestService.addDispatch("site2", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(2);
    assertThat(Helper.countItemsInDispatchRequest(SITE2_PENDING_DISPATCH)).isEqualTo(0);
  }

  /**
   * Site3 has an existing pending & new dispatch request. Adding a needed item
   * should be added to the new dispatch request, not the pending.
   */
  @Test
  void site3_addDispatch() {
    dispatchRequestService.addDispatch("site3", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH)).isEqualTo(1);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_NEW_DISPATCH)).isEqualTo(0);

    dispatchRequestService.addDispatch("site3", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_PENDING_DISPATCH)).isEqualTo(2);
    assertThat(Helper.countItemsInDispatchRequest(SITE3_NEW_DISPATCH)).isEqualTo(0);
  }

  /** Site4 has no dispatch requests, adding a needed item should create a brand new dispatch request. */
  @Test
  void site4_brandNewDispatch() {
    dispatchRequestService.addDispatch("site4", "water", ItemStatus.NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(1);
    dispatchRequestService.addDispatch("site4", "gloves", ItemStatus.URGENTLY_NEEDED);
    assertThat(Helper.countItemsInDispatchRequest(TEST_DISPATCH)).isEqualTo(2);
  }
}
