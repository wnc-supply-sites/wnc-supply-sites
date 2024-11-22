package com.vanatta.helene.supplies.database.supplies;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SiteSupplyResponse {
  int resultCount;
  List<SiteSupplyData> results;

  @Value
  @Builder
  static class SiteSupplyData {
    Long id;
    String site;
    String siteType;
    String county;
    @Builder.Default List<SiteItem> items = new ArrayList<>();
    boolean acceptingDonations;
    String lastUpdated;
  }

  @Value
  @Builder
  static class SiteItem {
    String name;
    String displayClass;

    public String getDisplayClass() {
      if (List.of("urgent", "needed", "available", "oversupply").contains(displayClass)) {
        return displayClass;
      } else {
        new Exception().printStackTrace();
        throw new IllegalStateException("Illegal display class: " + displayClass);
      }
    }
  }
}
