package com.vanatta.helene.supplies.database.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.Test;

class DriverControllerTest {

  String exampleInput =
      """
     {
        "airtableId":34,
        "fullName":"Test Driver",
        "phone":"(919) 111-1111",
        "active":true,
        "location":"nowheresville"
      }
  """;

  @Test
  void parseDriver() {
    var driver = DriverDao.Driver.parseJson(exampleInput);
    assertThat(driver.getAirtableId()).isEqualTo(34);
    assertThat(driver.getFullName()).isEqualTo("Test Driver");
    assertThat(driver.getPhone()).isEqualTo("(919) 111-1111");
    assertThat(driver.isActive()).isEqualTo(true);
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
  }

  @Test
  void upsert() {
    DriverController driverController = new DriverController(TestConfiguration.jdbiTest);
    driverController.receiveDriverUpdates(exampleInput);

    DriverDao.Driver driver =
        DriverDao.lookupByPhone(TestConfiguration.jdbiTest, "(919) 111-1111").orElseThrow();
    assertThat(driver.getFullName()).isEqualTo("Test Driver");
    assertThat(driver.getPhone()).isEqualTo("(919) 111-1111");
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
    assertThat(driver.isActive()).isEqualTo(true);
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
  }
}
