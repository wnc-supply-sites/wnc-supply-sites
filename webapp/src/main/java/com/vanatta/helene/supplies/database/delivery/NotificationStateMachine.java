package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.delivery.DeliveryConfirmation.ConfirmRole;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;

/**
 * Module to determine what SMS messages should be sent depending upon delivery state (confirmation)
 * changes.
 */
class NotificationStateMachine {

  @Builder
  @Value
  static class SmsMessage {
    @Nonnull String phone;
    @Nonnull String message;
  }

  public static List<SmsMessage> requestConfirmations(Delivery delivery) {
    List<SmsMessage> messages = new ArrayList<>();
    String messageTemplate =
        """
        WNC-supply-sites delivery requested. Please confirm.
        Delivery #%s, Date: %s
        View details and confirm with this link:
        %s
        """;
    messages.add(
        SmsMessage.builder()
            .phone(delivery.getDriverPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    DeliveryController.buildDeliveryPageLink(delivery, ConfirmRole.DRIVER)))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getFromContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    DeliveryController.buildDeliveryPageLink(delivery, ConfirmRole.PICKUP_SITE)))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getToContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    DeliveryController.buildDeliveryPageLink(delivery, ConfirmRole.DROPOFF_SITE)))
            .build());
    return messages;
  }

  static List<SmsMessage> confirm(Delivery delivery) {
    if (delivery.isConfirmed()) {
      // fully confirmed, send a message to everyone!
      String messageToDriver =
          String.format(
              """
      Delivery #%s confirmed for %s
      View the delivery and notify us
      when you get started with this link:
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
              DeliveryController.buildDeliveryPageLinkForDriver(delivery));
      String messageToOthers =
          String.format(
              """
      Delivery #%s confirmed for %s
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
              DeliveryController.buildDeliveryPageLink(delivery.getPublicKey()));

      List<SmsMessage> messages = new ArrayList<>();
      messages.add(
          SmsMessage.builder()
              .phone(delivery.getDriverPhoneNumber())
              .message(messageToDriver)
              .build());

      messages.addAll(
          Stream.of(
                  delivery.getDispatcherPhoneNumber(),
                  delivery.getToContactPhoneNumber(),
                  delivery.getFromContactPhoneNumber())
              .map(number -> SmsMessage.builder().phone(number).message(messageToOthers).build())
              .toList());
      return messages;
    } else {
      // send a confirmation received notification to just the dispatcher
      return List.of(
          SmsMessage.builder()
              .phone(delivery.getDispatcherPhoneNumber())
              .message(
                  String.format(
                      """
              Confirmation received, delivery #%s,
              Driver: %s - %s
              Pickup: %s - %s
              DropOff: %s - %s
              """,
                      delivery.getDeliveryNumber(),
                      delivery.getDriverName(),
                      Optional.ofNullable(
                              delivery
                                  .getConfirmation(ConfirmRole.DRIVER)
                                  .orElseThrow()
                                  .getConfirmed())
                          .map(Object::toString)
                          .orElse(""),
                      delivery.getFromSite(),
                      Optional.ofNullable(
                              delivery
                                  .getConfirmation(ConfirmRole.PICKUP_SITE)
                                  .orElseThrow()
                                  .getConfirmed())
                          .map(Object::toString)
                          .orElse(""),
                      delivery.getToSite(),
                      Optional.ofNullable(
                              delivery
                                  .getConfirmation(ConfirmRole.DROPOFF_SITE)
                                  .orElseThrow()
                                  .getConfirmed())
                          .map(Object::toString)
                          .orElse("")))
              .build());
    }
  }

  static List<SmsMessage> cancel(Delivery delivery) {
    if (delivery.getConfirmations().isEmpty()) {
      return List.of();
    } else {
      return Stream.of(
              delivery.getDispatcherPhoneNumber(),
              delivery.getDriverPhoneNumber(),
              delivery.getToContactPhoneNumber(),
              delivery.getFromContactPhoneNumber())
          .map(
              number ->
                  SmsMessage.builder()
                      .phone(number)
                      .message(
                          String.format(
                              """
                        Delivery #%s for date:%s is CANCELLED.
                        %s
                        """,
                              delivery.getDeliveryNumber(),
                              delivery.getDeliveryDate(),
                              DeliveryController.buildDeliveryPageLink(delivery.getPublicKey())))
                      .build())
          .toList();
    }
  }

  static List<SmsMessage> driverEnRoute(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getFromContactPhoneNumber())
        .map(number -> SmsMessage.builder().phone(number).message("driver en route").build())
        .toList();
  }

  static List<SmsMessage> driverArrivedToPickup(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getFromContactPhoneNumber())
        .map(number -> SmsMessage.builder().phone(number).message("arrived at pickup").build())
        .toList();
  }

  static List<SmsMessage> driverLeavingPickup(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getToContactPhoneNumber())
        .map(number -> SmsMessage.builder().phone(number).message("leaving pickup").build())
        .toList();
  }

  static List<SmsMessage> driverArrivedToDropOff(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getToContactPhoneNumber())
        .map(number -> SmsMessage.builder().phone(number).message("arrived at drop off").build())
        .toList();
  }
}
