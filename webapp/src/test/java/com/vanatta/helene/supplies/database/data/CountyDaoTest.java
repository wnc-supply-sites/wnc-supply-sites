package com.vanatta.helene.supplies.database.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class CountyDaoTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void fetchFullCountyList() {
    List<String> fullCountyList = CountyDao.fetchFullCountyList(TestConfiguration.jdbiTest);
    assertThat(fullCountyList).isNotEmpty();
    assertThat(fullCountyList).contains("dummy");
  }

  /**
   * Active counties are just the ones that have an active site. The list of active counties should
   * be shorter than the list of all counties.
   */
  @Test
  void fetchActiveCountyList() {
    int fullCountyCount =  CountyDao.fetchFullCountyList(TestConfiguration.jdbiTest).size();
    List<String> activeCounties =  CountyDao.fetchActiveCountyList(TestConfiguration.jdbiTest);

    assertThat(activeCounties.size()).isGreaterThan(0);
    assertThat(fullCountyCount).isGreaterThan(activeCounties.size());

    assertThat(activeCounties).contains("Watauga");
    assertThat(activeCounties).doesNotContain("dummy");
  }
}
