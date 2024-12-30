package com.vanatta.helene.supplies.database.delivery;

import jakarta.annotation.Nonnull;
import java.util.List;
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

  static List<SmsMessage> confirm(Delivery delivery, String code) {
    if (delivery.isConfirmed()) {
      return Stream.of(
              delivery.getDispatcherPhoneNumber(),
              delivery.getDriverPhoneNumber(),
              delivery.getToContactPhoneNumber(),
              delivery.getFromContactPhoneNumber())
          .map(number -> SmsMessage.builder().phone(number).message("full confirm").build())
          .toList();
    } else if (delivery.getConfirmations().isEmpty()) {
      return Stream.of(
              delivery.getDriverPhoneNumber(),
              delivery.getToContactPhoneNumber(),
              delivery.getFromContactPhoneNumber())
          .map(
              number ->
                  SmsMessage.builder()
                      .phone(number)
                      .message("new delivery confirm request")
                      .build())
          .toList();
    } else {
      return List.of(
          SmsMessage.builder()
              .phone(delivery.getDispatcherPhoneNumber())
              .message("confirm received")
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
          .map(number -> SmsMessage.builder().phone(number).message("cancel").build())
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
