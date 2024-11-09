package com.vanatta.helene.supplies.database.supplies;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SiteSupplyResponse {
  int resultCount;
  List<SiteSupplyData> results;

  @Value
  @Builder
  static class SiteSupplyData {
    String site;
    String county;
    List<SiteItem> items;
  }

  @Value
  @Builder
  static class SiteItem {
    String name;
    String status;
  }
}
