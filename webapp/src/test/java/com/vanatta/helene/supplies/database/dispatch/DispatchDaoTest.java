package com.vanatta.helene.supplies.database.dispatch;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DispatchDaoTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
    ;
  }

  @Test
  void nextDispatchNumber() {
    long first = DispatchDao.nextDispatchNumber(TestConfiguration.jdbiTest);
    long second = DispatchDao.nextDispatchNumber(TestConfiguration.jdbiTest);

    // verify that we get numbers in increasing sequence order
    assertThat(first).isLessThan(second);
  }

  @Test
  void recordDispatchRequest() {
    String publicId = UUID.randomUUID().toString();
    var dispatchRequest =
        DispatchRequestService.DispatchRequestJson.builder()
            .needRequestId(publicId)
            .requestingSite("site1")
            .items(List.of("water"))
            .priority(ItemStatus.NEEDED.getText())
            .build();
    long dispatchId = DispatchDao.recordNewDispatch(TestConfiguration.jdbiTest, -1L, dispatchRequest);

    var fetchedPublicId = DispatchDao.fetchDispatchPublicId(TestConfiguration.jdbiTest, dispatchId);
    assertThat(fetchedPublicId).isEqualTo(publicId);

    var fetchedDispatchId =
        DispatchDao.lookupDispatchRequestId(TestConfiguration.jdbiTest, "site1", "water");
    assertThat(fetchedDispatchId).isEqualTo(dispatchId);

    long sendRequestId =
        DispatchDao.storeSendRequest(TestConfiguration.jdbiTest, dispatchId, "NEW");
    DispatchDao.completeSendRequest(TestConfiguration.jdbiTest, sendRequestId);
  }

}
