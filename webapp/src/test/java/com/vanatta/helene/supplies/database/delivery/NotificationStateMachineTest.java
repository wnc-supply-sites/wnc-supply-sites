package com.vanatta.helene.supplies.database.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationStateMachineTest {

  static final String dispatcherNumber = "0000";
  static final String driverNumber = "1111";
  static final String pickupNumber = "2222";
  static final String dropOffNumber = "3333";

  static final String dispatchCode = "AAAA";
  static final String driverCode = "BBBB";
  static final String pickupCode = "CCCC";
  static final String dropOffCode = "DDDD";

  static final Delivery sample =
      Delivery.builder()
          .dispatchCode(dispatchCode)
          .driverPhoneNumber(driverNumber)
          .dispatcherPhoneNumber(dispatcherNumber)
          .fromContactPhoneNumber(pickupNumber)
          .toContactPhoneNumber(dropOffNumber)
          .build();

  static final Delivery withPendingConfirmations =
      sample.toBuilder()
          .confirmations(
              List.of(
                  DeliveryConfirmation.builder()
                      .code(driverCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.DRIVER.name())
                      .build(),
                  DeliveryConfirmation.builder()
                      .code(pickupCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.PICKUP_SITE.name())
                      .build(),
                  DeliveryConfirmation.builder()
                      .code(dropOffCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.DROPOFF_SITE.name())
                      .build()))
          .build();

  static final Delivery withFullyConfirmed =
      sample.toBuilder()
          .confirmations(
              List.of(
                  DeliveryConfirmation.builder()
                      .code(driverCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.DRIVER.name())
                      .confirmed(true)
                      .build(),
                  DeliveryConfirmation.builder()
                      .code(pickupCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.PICKUP_SITE.name())
                      .confirmed(true)
                      .build(),
                  DeliveryConfirmation.builder()
                      .code(dropOffCode)
                      .confirmRole(DeliveryConfirmation.ConfirmRole.DROPOFF_SITE.name())
                      .confirmed(true)
                      .build()))
          .build();

  /** When dispatcher confirms we send a confirmation request to driver & sites */
  @Test
  void confirm_dispatcherConfirm() {
    var results = NotificationStateMachine.confirm(sample, dispatchCode);

    // confirmation request to driver & sites
    assertThat(results).hasSize(3);
    assertPhoneNumbers(results, driverNumber, pickupNumber, dropOffNumber);
  }

  private static void assertPhoneNumbers(
      List<NotificationStateMachine.SmsMessage> results, String... phoneNumbers) {
    List<String> sortedNumbers = List.of(phoneNumbers).stream().sorted().toList();
    assertThat(
            results.stream().map(NotificationStateMachine.SmsMessage::getPhone).sorted().toList())
        .isEqualTo(sortedNumbers);
  }

  @ParameterizedTest
  @ValueSource(strings = {driverCode, pickupCode, dropOffCode})
  void confirm_singleConfirmation(String code) {
    var results = NotificationStateMachine.confirm(withPendingConfirmations, dispatchCode);
    assertPhoneNumbers(results, dispatcherNumber);
  }

  @Test
  void confirm_fullyConfirmed() {
    var results = NotificationStateMachine.confirm(withFullyConfirmed, dispatchCode);
    assertPhoneNumbers(results, dispatcherNumber, driverNumber, pickupNumber, dropOffNumber);
  }

  /** If we never started the confirmation process, then a cancel does not need to notify anyone */
  @Test
  void cancel_dispatcherNeverConfirmed() {
    var results = NotificationStateMachine.cancel(sample);
    assertThat(results).isEmpty();
  }

  /** Once confirmations start, a cancel should notify everyone */
  @Test
  void cancel_confirmationsStarted() {
    var results = NotificationStateMachine.cancel(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, driverNumber, pickupNumber, dropOffNumber);
  }

  @Test
  void driverEnRoute() {
    var results = NotificationStateMachine.driverEnRoute(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, pickupNumber);
  }

  @Test
  void driverArrivedToPickup() {
    var results = NotificationStateMachine.driverArrivedToPickup(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, pickupNumber);
  }

  @Test
  void driverLeavingPickup() {
    var results = NotificationStateMachine.driverLeavingPickup(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, dropOffNumber);
  }

  @Test
  void driverArrivedToDropOff() {
    var results = NotificationStateMachine.driverArrivedToDropOff(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, dropOffNumber);
  }
}
