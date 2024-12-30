package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.data.GoogleDistanceApi;
import com.vanatta.helene.supplies.database.delivery.DeliveryConfirmation.ConfirmRole;
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
        Delivery #%s, Date: %s
        View details and confirm with this link:
        %s
        Items (%s): %s
        """;
    messages.add(
        SmsMessage.builder()
            .phone(delivery.getDriverPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(delivery, ConfirmRole.DRIVER),
                    delivery.getItemCount(),
                    delivery.getItemListTruncated()))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getFromContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(
                            delivery, ConfirmRole.PICKUP_SITE),
                    delivery.getItemCount(),
                    delivery.getItemListTruncated()))
            .build());

    messages.add(
        SmsMessage.builder()
            .phone(delivery.getToContactPhoneNumber())
            .message(
                String.format(
                    messageTemplate,
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveryDate(),
                    websiteUri
                        + DeliveryController.buildDeliveryPageLink(
                            delivery, ConfirmRole.DROPOFF_SITE),
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
      View the delivery and notify us
      when you get started with this link:
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
              websiteUri + DeliveryController.buildDeliveryPageLinkForDriver(delivery));
      String messageToOthers =
          String.format(
              """
      Delivery #%s confirmed for %s
      %s
      """,
              delivery.getDeliveryNumber(),
              delivery.getDeliveryDate(),
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
                        Delivery #%s for date:%s is CANCELLED.
                        %s
                        """,
                              delivery.getDeliveryNumber(),
                              delivery.getDeliveryDate(),
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
                        Driver, %s, is now en route to %s, license plate: %s, to pick up %s items.
                        Full Details: %s
                        """,
                            delivery.getDriverName(),
                            delivery.getToSite(),
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
                Driver, %s, has arrived at %s, license plate: %s, to pick up %s items.
                Full Details: %s
                """,
                            delivery.getDriverName(),
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
                Inbound driver ETA: %s, driver %s is now on their way to %s,
                they just left %s in %s, transporting %s items.
                License plates: %s
                Full Details: %s
                """,
                            googleDistanceApi.estimateEta(delivery),
                            delivery.getDriverName(),
                            delivery.getToSite(),
                            delivery.getFromSite(),
                            delivery.getFromCity(),
                            delivery.getItemCount(),
                            delivery.getDriverLicensePlate(),
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
          Supplies driver %s, license plates: %s, has arrived at drop off site: %s
          #wncStrong
          """,
                            delivery.getDriverName(),
                            delivery.getDriverLicensePlate(),
                            delivery.getToSite()))
                    .build())
        .toList();
  }
}
