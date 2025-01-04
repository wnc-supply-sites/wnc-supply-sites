package com.vanatta.helene.supplies.database.driver;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DriverControllerTest {

  DriverController driverController = new DriverController(jdbiTest, SendDriverUpdate.disabled());
  Driver driver = TestConfiguration.buildDriver(-103L, "123-123-4444");
  
  @BeforeEach
  void setup() {
    TestConfiguration.setupDatabase();
    DriverDao.upsert(jdbiTest, driver);
  }
  
  
  @Test
  void renderPage() {
    var modelAndView = driverController.showDriverPortal("123-123-4444");
    Arrays.stream(DriverController.PageParams.values())
        .forEach(
            param -> assertThat(modelAndView.getModelMap().getAttribute(param.name())).isNotNull());
    assertThat(
            modelAndView
                .getModelMap()
                .getAttribute(DriverController.PageParams.availability.name()))
        .isEqualTo(driver.getAvailability());
    assertThat(modelAndView.getModelMap().getAttribute(DriverController.PageParams.comments.name()))
        .isEqualTo(driver.getComments());
    assertThat(modelAndView.getModelMap().getAttribute(DriverController.PageParams.location.name()))
        .isEqualTo(driver.getLocation());
    assertThat(
            modelAndView
                .getModelMap()
                .getAttribute(DriverController.PageParams.licensePlates.name()))
        .isEqualTo(driver.getLicensePlates());
    assertThat(modelAndView.getModelMap().getAttribute(DriverController.PageParams.active.name()))
        .isEqualTo(driver.isActive());
  }

  @Test
  void updateDriver() {
    Map<String, String> params = new HashMap<>();
    params.put(DriverController.PageParams.comments.name(), "comments demo");
    params.put(DriverController.PageParams.location.name(), "location demo");
    params.put(DriverController.PageParams.licensePlates.name(), "plates demo");
    params.put(DriverController.PageParams.availability.name(), "availability demo");

    var response = driverController.updateDriver("123-123-4444", params);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    var dataResult = DriverDao.lookupByPhone(jdbiTest, driver.getPhone()).orElseThrow();
    assertThat(dataResult.getComments()).isEqualTo("comments demo");
    assertThat(dataResult.getLocation()).isEqualTo("location demo");
    assertThat(dataResult.getLicensePlates()).isEqualTo("plates demo");
    assertThat(dataResult.getAvailability()).isEqualTo("availability demo");
  }
  
  
  /**
   * For a given driver in the database, flip their active flag. Reload the driver from database again
   * and then assert that their active flag has changed. Then toggle the active flag again and
   * assert that the active status has changed once more.
   */
  @Test
  void changeDriverActiveStatus() {
    boolean active = driver.isActive();
    
    driverController.changeDriverActiveStatus(driver.getPhone());
    
    var dataResult = DriverDao.lookupByPhone(jdbiTest, driver.getPhone()).orElseThrow();
    assertThat(dataResult.isActive()).isNotEqualTo(active);
    
    driverController.changeDriverActiveStatus(driver.getPhone());
    dataResult = DriverDao.lookupByPhone(jdbiTest, driver.getPhone()).orElseThrow();
    assertThat(dataResult.isActive()).isEqualTo(active);
  }
}
