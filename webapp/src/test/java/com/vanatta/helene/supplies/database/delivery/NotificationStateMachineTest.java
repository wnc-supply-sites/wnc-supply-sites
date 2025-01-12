package com.vanatta.helene.supplies.database.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.data.GoogleDistanceApi;
import com.vanatta.helene.supplies.database.data.SiteAddress;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationStateMachineTest {
  final String domain = "WNC-supply-sites.com";
  NotificationStateMachine notificationStateMachine =
      new NotificationStateMachine(
          new GoogleDistanceApi("") {
            @Override
            public GoogleDistanceResponse queryDistance(SiteAddress from, SiteAddress to) {
              return GoogleDistanceResponse.builder().distance(10.0).duration(3600L).build();
            }
          });

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
    var results =
        notificationStateMachine.requestConfirmations(
            withPendingConfirmations.toBuilder()
                .deliveryNumber(23)
                .publicKey("AAAA")
                .deliveryDate("Dec 12")
                .toSite("to site")
                .toCity("to city")
                .itemList(List.of("water", "soap"))
                .build(),
            domain);

    // confirmation request to driver & sites
    assertThat(results).hasSize(3);
    assertPhoneNumbers(results, driverNumber, pickupNumber, dropOffNumber);

    assertThat(results.getFirst().getMessage())
        .isEqualTo(
            """
          WNC-supply-sites.com delivery requested. Please confirm.
          https://WNC-supply-sites.com/delivery/AAAA?code=BBBB

          Delivery #23
          Date: Dec 12
          Heading to: to site, to city
          Items (2): soap, water
          """);
  }

  private static void assertPhoneNumbers(
      List<NotificationStateMachine.SmsMessage> results, String... phoneNumbers) {
    List<String> sortedNumbers = List.of(phoneNumbers).stream().sorted().toList();
    assertThat(
            results.stream().map(NotificationStateMachine.SmsMessage::getPhone).sorted().toList())
        .isEqualTo(sortedNumbers);
  }

  @Test
  void confirm_singleConfirmation() {
    var results =
        notificationStateMachine.confirm(
            withPendingConfirmations.toBuilder()
                .deliveryNumber(212)
                .driverName("The Driver")
                .fromSite("zTest")
                .toSite("zDrop")
                .itemList(List.of("water", "brushes"))
                .confirmations(
                    List.of(
                        DeliveryConfirmation.builder()
                            .confirmed(true)
                            .confirmRole(DeliveryConfirmation.ConfirmRole.DROPOFF_SITE.name())
                            .code("XKCD")
                            .build(),
                        DeliveryConfirmation.builder()
                            .confirmed(null)
                            .confirmRole(DeliveryConfirmation.ConfirmRole.PICKUP_SITE.name())
                            .code("XKCD")
                            .build(),
                        DeliveryConfirmation.builder()
                            .confirmed(null)
                            .confirmRole(DeliveryConfirmation.ConfirmRole.DRIVER.name())
                            .code("XKCD")
                            .build()))
                .build(),
            domain);
    assertPhoneNumbers(results, dispatcherNumber);

    assertThat(results.getFirst().getMessage())
        .isEqualTo(
            """
        Confirmation received.
        Delivery #212 (2 items)
        PENDING: The Driver (Driver)
        PENDING: zTest (Pickup)
        CONFIRMED: zDrop (DropOff)
        """);
  }

  @Test
  void confirm_fullyConfirmed() {
    var results = notificationStateMachine.confirm(withFullyConfirmed, domain);
    assertPhoneNumbers(results, dispatcherNumber, driverNumber, pickupNumber, dropOffNumber);
  }

  /** If we never started the confirmation process, then a cancel does not need to notify anyone */
  @Test
  void cancel_dispatcherNeverConfirmed() {
    var results = notificationStateMachine.cancel(sample, domain);
    assertThat(results).isEmpty();
  }

  /** Once confirmations start, a cancel should notify everyone */
  @Test
  void cancel_confirmationsStarted() {
    var results = notificationStateMachine.cancel(withPendingConfirmations, domain);
    assertPhoneNumbers(results, dispatcherNumber, driverNumber, pickupNumber, dropOffNumber);
  }

  @Test
  void driverEnRoute() {
    var results = notificationStateMachine.driverEnRoute(withPendingConfirmations, domain);
    assertPhoneNumbers(results, dispatcherNumber, pickupNumber);
  }

  @Test
  void driverArrivedToPickup() {
    var results = notificationStateMachine.driverArrivedToPickup(withPendingConfirmations, domain);
    assertPhoneNumbers(results, dispatcherNumber, pickupNumber);
  }

  @Test
  void driverLeavingPickup() {
    var results =
        notificationStateMachine.driverLeavingPickup(
            withPendingConfirmations.toBuilder()
                .fromState("NC")
                .fromCity("Black Rock")
                .fromAddress("Hell Ya")
                .toState("NC")
                .toCity("Elk Park")
                .toAddress("Main St.")
                .build(),
            domain);
    assertPhoneNumbers(results, dispatcherNumber, dropOffNumber);
  }

  @Test
  void driverArrivedToDropOff() {
    var results = NotificationStateMachine.driverArrivedToDropOff(withPendingConfirmations);
    assertPhoneNumbers(results, dispatcherNumber, dropOffNumber);
  }
}
