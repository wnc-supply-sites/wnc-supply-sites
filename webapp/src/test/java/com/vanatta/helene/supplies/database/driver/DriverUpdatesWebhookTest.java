package com.vanatta.helene.supplies.database.driver;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DriverUpdatesWebhookTest {

  @BeforeAll
  static void cleanDb() {
    TestConfiguration.setupDatabase();
  }

  private final DriverUpdatesWebhook driverUpdatesWebhook = new DriverUpdatesWebhook(jdbiTest);

  static final String phoneNumber = "(919) 111-1111";

  static final String newDriverInput =
      """
   {"airtableId":48,"fullName":"Test2 Test",
   "phone":"(919) 111-1111","location":"nowheresville",
   "comments":"comments from driver", "active": true, blacklisted: null,
   "availability":"sometimes","licensePlates":"licensePlates"}
  """;

  @Test
  void parseDriver() {
    var driver = Driver.parseJson(newDriverInput);
    assertThat(driver.getAirtableId()).isEqualTo(48);
    assertThat(driver.getFullName()).isEqualTo("Test2 Test");
    assertThat(driver.getPhone()).isEqualTo("(919) 111-1111");
    assertThat(driver.getLocation()).isEqualTo("nowheresville");
    assertThat(driver.getComments()).isEqualTo("comments from driver");
    assertThat(driver.getAvailability()).isEqualTo("sometimes");
    assertThat(driver.getLicensePlates()).isEqualTo("licensePlates");
  }

  @Test
  void upsert() {
    var driver = Driver.parseJson(newDriverInput);

    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);

    Driver resultFromDb = DriverDao.lookupByPhone(jdbiTest, driver.getPhone()).orElseThrow();
    assertThat(resultFromDb.getFullName()).isEqualTo(driver.getFullName());
    assertThat(resultFromDb.getPhone())
        .isEqualTo(PhoneNumberUtil.removeNonNumeric(driver.getPhone()));
    assertThat(resultFromDb.getLocation()).isEqualTo(driver.getLocation());
    assertThat(resultFromDb.getComments()).isEqualTo(driver.getComments());
    assertThat(resultFromDb.getAvailability()).isEqualTo(driver.getAvailability());
    assertThat(resultFromDb.getLicensePlates()).isEqualTo(driver.getLicensePlates());
    assertThat(resultFromDb.isActive()).isEqualTo(true);
    assertThat(resultFromDb.isBlacklisted()).isEqualTo(false);
  }

  static final String blackListFalse =
      """
  {"airtableId":48,"fieldName":"blacklisted","newValue":null}
  """;

  static final String blackListTrue =
      """
  {"airtableId":48,"fieldName":"blacklisted","newValue":true}
  """;

  static final String activeTrue =
      """
  {"airtableId":48,"fieldName":"active","newValue":true}
  """;

  static final String activeFalse =
      """
  {"airtableId":48,"fieldName":"active","newValue":null}
  """;

  static final String licensePlateUpdate =
      """
  {"airtableId":48,"fieldName":"licensePlates","newValue":"XXXX-123"}
  """;
  static final String licensePlateUpdateToNull =
      """
  {"airtableId":48,"fieldName":"licensePlates","newValue":null}
  """;

  @Test
  void blacklistFalse() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(blackListFalse);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().isBlacklisted())
        .isFalse();
  }

  @Test
  void blacklistTrue() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(blackListTrue);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().isBlacklisted())
        .isTrue();
  }

  @Test
  void activeTrue() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(activeTrue);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().isActive()).isTrue();
  }

  @Test
  void activeFalse() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(activeFalse);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().isActive()).isFalse();
  }

  @Test
  void setLicensePlateUpdate() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(licensePlateUpdate);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().getLicensePlates())
        .isEqualTo("XXXX-123");
  }

  @Test
  void licensePlateToNull() {
    driverUpdatesWebhook.receiveDriverUpdates(newDriverInput);
    driverUpdatesWebhook.receiveDriveFieldUpdate(licensePlateUpdateToNull);

    assertThat(DriverDao.lookupByPhone(jdbiTest, phoneNumber).orElseThrow().getLicensePlates())
        .isNull();
  }
}
