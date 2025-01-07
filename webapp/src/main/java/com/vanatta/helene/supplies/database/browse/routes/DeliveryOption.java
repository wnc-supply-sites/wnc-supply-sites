package com.vanatta.helene.supplies.database.browse.routes;

import com.vanatta.helene.supplies.database.data.SiteAddress;
import com.vanatta.helene.supplies.database.util.DurationFormatter;
import com.vanatta.helene.supplies.database.util.ListSplitter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class DeliveryOption {
  @Value
  @Builder
  static class Item {
    String name;
    String urgencyCssClass;
    long wssId;
  }

  String fromSiteName;
  long fromSiteWssId;
  String fromSiteLink;
  String fromAddress;
  String fromCity;
  String fromCounty;
  String fromState;
  String fromHours;

  String toSiteName;
  long toSiteWssId;
  String toSiteLink;
  String toAddress;
  String toCity;
  String toCounty;
  String toState;
  String toHours;

  Integer driveTimeSeconds;
  Double distanceMiles;
  @Builder.Default List<Item> items = new ArrayList<>();

  List<Long> getItemWssIds() {
    return getItems().stream().map(Item::getWssId).sorted().toList();
  }

  /** lower numbers sort first */
  double sortScore() {
    if (distanceMiles == null) {
      return 1000.0 - items.size();
    } else {
      return distanceMiles;
    }
  }

  String getFromHours() {
    return formatHours(fromHours);
  }

  String getToHours() {
    return formatHours(toHours);
  }

  private static String formatHours(String hours) {
    if (hours == null) {
      return null;
    } else {
      return hours.replaceAll("\n", "<br>");
    }
  }

  String getFromGoogleMapsAddress() {
    return SiteAddress.builder()
        .address(fromAddress)
        .city(fromCity)
        .state(fromState)
        .build()
        .toEncodedUrlValue();
  }

  String getGoogleMapsAddress() {
    return SiteAddress.builder()
        .address(toAddress)
        .city(toCity)
        .state(toState)
        .build()
        .toEncodedUrlValue();
  }

  String getDriveTime() {
    return driveTimeSeconds == null
        ? null
        : DurationFormatter.formatDuration(Duration.ofSeconds(driveTimeSeconds));
  }

  List<Item> getItems1() {
    List<List<Item>> splitLists =
        ListSplitter.splitItemList(
            items.stream().sorted(Comparator.comparing(i -> i.name)).toList(), 5);
    return splitLists.getFirst();
  }

  List<Item> getItems2() {
    List<List<Item>> splitLists =
        ListSplitter.splitItemList(
            items.stream().sorted(Comparator.comparing(i -> i.name)).toList(), 5);
    return splitLists.size() > 1 ? splitLists.get(1) : List.of();
  }

  void addItem(Item item) {
    this.items.add(item);
  }

  int getItemCount() {
    return items.size();
  }
}
