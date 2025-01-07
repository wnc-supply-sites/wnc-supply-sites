package com.vanatta.helene.supplies.database.browse.routes;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.browse.routes.RouteVolunteeringController.DeliveryVolunteerRequest;
import com.vanatta.helene.supplies.database.driver.DriverDao;
import java.util.List;
import org.junit.jupiter.api.Test;

class RouteVolunteeringControllerTest {

  @Test
  void volunteer() {
    DriverDao.upsert(jdbiTest, TestConfiguration.buildDriver(-6532L, "666.999.4444"));

    DeliveryVolunteerRequest json =
        RouteVolunteeringController.createVolunteeringRequestJson(
            jdbiTest,
            DeliveryVolunteerRequest.builder()
                .fromSiteWssId(123L)
                .toSiteWssId(333L)
                .itemList(List.of(44L, 55L, 66L))
                .fromDate("Dec-30")
                .toDate("Jan-04")
                .build(),
            "666.999.4444");

    assertThat(json.getFromSiteWssId()).isEqualTo(123L);
    assertThat(json.getToSiteWssId()).isEqualTo(333L);
    assertThat(json.getItemList()).containsExactly(44L, 55L, 66L);
    assertThat(json.getFromDate()).isEqualTo("Dec-30");
    assertThat(json.getToDate()).isEqualTo("Jan-04");
    assertThat(json.getDriverAirtableId()).isEqualTo(-6532L);
  }
}
