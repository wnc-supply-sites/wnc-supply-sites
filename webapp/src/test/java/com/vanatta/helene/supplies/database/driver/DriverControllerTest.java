package com.vanatta.helene.supplies.database.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DriverControllerTest {

  String exampleInput =
      """
     {
        "airtableId":34,
        "fullName":"Test Driver",
        "email":"exampleEmail",
        "phone":"(919) 111-1111",
        "active":true,
        "location":"nowheresville"
      }
  """;

  @Test
  void parseDriver() {
    DriverController.DriverJson driver = DriverController.DriverJson.parseJson(exampleInput);
    assertThat(driver.getAirtableId()).isEqualTo(34);
    assertThat(driver.getFullName()).isEqualTo("Test Driver");
    assertThat(driver.getEmail()).isEqualTo("exampleEmail");
    assertThat(driver.getPhone()).isEqualTo("(919) 111-1111");
    assertThat(driver.isActive()).isEqualTo(true);
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
  }

  @Disabled // TODO
  @Test
  void upsert() {
    DriverController driverController = new DriverController(TestConfiguration.jdbiTest);
    driverController.receiveDriverUpdates(exampleInput);

    DriverDao.Driver driver = DriverDao.lookupByAirtableId(34L);
    assertThat(driver.getFullName()).isEqualTo("Test Driver");
    assertThat(driver.getEmail()).isEqualTo("exampleEmail");
    assertThat(driver.getPhone()).isEqualTo("(919) 111-1111");
    assertThat(driver.isActive()).isEqualTo(true);
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
  }
}
