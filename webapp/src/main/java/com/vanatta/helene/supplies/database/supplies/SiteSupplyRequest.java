package com.vanatta.helene.supplies.database.supplies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteSupplyRequest {
  // how many different item statuses are there in total.
  public static final int ITEM_STATUS_COUNT = ItemStatus.values().length;

  @Getter
  @AllArgsConstructor
  public enum ItemStatus {
    URGENTLY_NEEDED("Urgently Needed"),
    NEEDED("Needed"),
    AVAILABLE("Available"),
    OVERSUPPLY("Oversupply"),
    ;
    private final String text;

    public static List<String> allItemStatus() {
      return Arrays.stream(values()).map(s -> s.text).toList();
    }
  }

  // how many different site types in total
  public static final int SITE_TYPE_COUNT = SiteType.values().length;

  @Getter
  @AllArgsConstructor
  public enum SiteType {
    DISTRIBUTION_CENTER("Distribution Center"),
    SUPPLY_HUB("Supply Hub"),
    ;
    private final String text;

    static List<String> allSiteTypes() {
      return Arrays.stream(values()).map(s -> s.text).toList();
    }
  }

  @Builder.Default List<String> sites = new ArrayList<>();
  @Builder.Default List<String> items = new ArrayList<>();
  @Builder.Default List<String> counties = new ArrayList<>();
  @Builder.Default List<String> itemStatus = new ArrayList<>();
  @Builder.Default List<String> siteType = new ArrayList<>();
  @Builder.Default Boolean acceptingDonations = true;
  @Builder.Default Boolean notAcceptingDonations = true;
}
