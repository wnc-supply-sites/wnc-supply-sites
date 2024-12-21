package com.vanatta.helene.supplies.database.jobs.distance;

import static org.assertj.core.api.Assertions.*;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.GoogleDistanceApi;
import com.vanatta.helene.supplies.database.data.SiteAddress;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistanceCalculatorTest {

  @BeforeEach
  void setup() {
    DistanceTestHelper.setup();
  }

  @Test
  void distanceCalculation() {

    /* distance API returns hardcoded values */
    DistanceCalculator calculator =
        new DistanceCalculator(
            TestConfiguration.jdbiTest,
            new GoogleDistanceApi("") {
              @Override
              public GoogleDistanceResponse queryDistance(SiteAddress from, SiteAddress to) {
                return GoogleDistanceResponse.builder()
                    .distance(100.1)
                    .duration(360L)
                    .valid(true)
                    .build();
              }
            },
            true,
            0);
    calculator.calculateDistances();

    long site1Id = TestConfiguration.getSiteId("site1");
    long site2Id = TestConfiguration.getSiteId("site4");

    Optional<DistanceDao.DistanceResult> result =
        DistanceDao.queryDistance(TestConfiguration.jdbiTest, site1Id, site2Id);
    assertThat(result).isPresent();
    assertThat(result.get().getDistance()).isEqualTo(100.1);
    assertThat(result.get().getDurationSeconds()).isEqualTo(360L);
  }

  @Test
  void distanceCalculationBadAddress() {
    /* Distance API calculator does not return data (invalid address case) */
    DistanceCalculator calculator =
        new DistanceCalculator(
            TestConfiguration.jdbiTest,
            new GoogleDistanceApi("") {
              @Override
              public GoogleDistanceResponse queryDistance(SiteAddress from, SiteAddress to) {
                return GoogleDistanceResponse.builder().valid(false).build();
              }
            },
            true,
            0);
    calculator.calculateDistances();

    long site2Id = TestConfiguration.getSiteId("site2");
    long site4Id = TestConfiguration.getSiteId("site4");
    Optional<DistanceDao.DistanceResult> result =
        DistanceDao.queryDistance(TestConfiguration.jdbiTest, site2Id, site4Id);
    assertThat(result).isEmpty();
  }
}
