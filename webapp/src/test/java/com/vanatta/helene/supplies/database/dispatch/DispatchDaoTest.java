package com.vanatta.helene.supplies.database.dispatch;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DispatchDaoTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void nextDispatchNumber() {
    long first = DispatchDao.nextDispatchNumber(TestConfiguration.jdbiTest);
    long second = DispatchDao.nextDispatchNumber(TestConfiguration.jdbiTest);

    // verify that we get numbers in increasing sequence order
    assertThat(first).isLessThan(second);
  }
}
