package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.data.GoogleDistanceApi;
import com.vanatta.helene.supplies.database.delivery.DeliveryConfirmation.ConfirmRole;
import com.vanatta.helene.supplies.database.util.TruncateString;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Module to determine what SMS messages should be sent depending upon delivery state (confirmation)
 * changes.
 */
@Component
class NotificationStateMachine {

  private final String websiteUri;
  private final GoogleDistanceApi googleDistanceApi;

  NotificationStateMachine(
      @Value("${website.uri}") String websiteUri, GoogleDistanceApi googleDistanceApi) {
    this.websiteUri = websiteUri;
    this.googleDistanceApi = googleDistanceApi;
  }

  @Builder
  @lombok.Value
  static class SmsMessage {
    @Nonnull String phone;
    @Nonnull String message;
  }

  List<SmsMessage> requestConfirmations(Delivery delivery) {
    List<SmsMessage> messages = new ArrayList<>();
    String messageTemplate =
        """
        WNC-supply-sites delivery requested. Please confirm.
        %s

        Delivery #%s
        Date: %s
        Heading to: %s, %s
        Items (%s): %s
        """;
    messages.add(
        SmsMessage.builder()
            .phone(delivery.getDriverPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(delivery, ConfirmRole.DRIVER),
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    delivery.getToSite(),
                    delivery.getToCity(),
                    delivery.getItemCount(),
                    delivery.getItemListTruncated()))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getFromContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(
                            delivery, ConfirmRole.PICKUP_SITE),
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    delivery.getToSite(),
                    delivery.getToCity(),
                    delivery.getItemCount(),
                    delivery.getItemListTruncated()))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getToContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(
                            delivery, ConfirmRole.DROPOFF_SITE),
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    delivery.getToSite(),
                    delivery.getToCity(),
                    delivery.getItemCount(),
                    delivery.getItemListTruncated()))
            .build());
    return messages;
  }

  List<SmsMessage> confirm(Delivery delivery) {
    if (delivery.isConfirmed()) {
      // fully confirmed, send a message to everyone!
      String messageToDriver =
          String.format(
              """
      Delivery #%s confirmed for %s
      Heading to: %s in %s
      View the delivery and notify us
      when you get started with this link:
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
              delivery.getToSite(),
              delivery.getToCity(),
              websiteUri + DeliveryController.buildDeliveryPageLinkForDriver(delivery));
      String messageToOthers =
          String.format(
              """
      Delivery #%s confirmed for %s
      Heading to: %s in %s
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
              delivery.getToSite(),
              delivery.getToCity(),
              websiteUri + DeliveryController.buildDeliveryPageLink(delivery.getPublicKey()));

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

  List<SmsMessage> cancel(Delivery delivery) {
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
                        Delivery #%s for date:%s is CANCELLED. %s
                        %s
                        """,
                              delivery.getDeliveryNumber(),
                              delivery.getDeliveryDate(),
                              delivery.getCancelReason() == null
                                      || delivery.getCancelReason().isBlank()
                                  ? ""
                                  : "\nReason: "
                                      + TruncateString.truncate(delivery.getCancelReason(), 96),
                              websiteUri
                                  + DeliveryController.buildDeliveryPageLink(
                                      delivery.getPublicKey())))
                      .build())
          .toList();
    }
  }

  List<SmsMessage> driverEnRoute(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getFromContactPhoneNumber())
        .map(
            number ->
                SmsMessage.builder()
                    .phone(number)
                    .message(
                        String.format(
                            """
                        Driver is on the way to: %s
                        Driver: %s, License plate: %s, to pick up %s items.
                        Full Details: %s
                        """,
                            delivery.getToSite(),
                            delivery.getDriverName(),
                            delivery.getDriverLicensePlate(),
                            delivery.getItemCount(),
                            websiteUri
                                + DeliveryController.buildDeliveryPageLink(
                                    delivery.getPublicKey())))
                    .build())
        .toList();
  }

  List<SmsMessage> driverArrivedToPickup(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getFromContactPhoneNumber())
        .map(
            number ->
                SmsMessage.builder()
                    .phone(number)
                    .message(
                        String.format(
                            """
                Driver has arrived at pickup %s
                License plate: %s, to pick up %s items.
                Full Details: %s
                """,
                            delivery.getToSite(),
                            delivery.getDriverLicensePlate(),
                            delivery.getItemCount(),
                            websiteUri
                                + DeliveryController.buildDeliveryPageLink(
                                    delivery.getPublicKey())))
                    .build())
        .toList();
  }

  List<SmsMessage> driverLeavingPickup(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getToContactPhoneNumber())
        .map(
            number ->
                SmsMessage.builder()
                    .phone(number)
                    .message(
                        String.format(
                            """
                Driver is on the way to the drop off site: %s
                ETA: %s
                Driver: %s
                License plates: %s
                They just left %s in %s, transporting %s items.
                Full Details: %s
                """,
                            delivery.getToSite(),
                            googleDistanceApi.estimateEta(delivery),
                            delivery.getDriverName(),
                            delivery.getDriverLicensePlate(),
                            delivery.getFromSite(),
                            delivery.getFromCity(),
                            delivery.getItemCount(),
                            websiteUri
                                + DeliveryController.buildDeliveryPageLink(
                                    delivery.getPublicKey())))
                    .build())
        .toList();
  }

  static List<SmsMessage> driverArrivedToDropOff(Delivery delivery) {
    return Stream.of(delivery.getDispatcherPhoneNumber(), delivery.getToContactPhoneNumber())
        .map(
            number ->
                SmsMessage.builder()
                    .phone(number)
                    .message(
                        String.format(
                            """
          Driver has arrived at the drop off site: %s
          Name: %s, license plate: %s
          #wncStrong
          """,
                            delivery.getToSite(),
                            delivery.getDriverName(),
                            delivery.getDriverLicensePlate()))
                    .build())
        .toList();
  }
}
