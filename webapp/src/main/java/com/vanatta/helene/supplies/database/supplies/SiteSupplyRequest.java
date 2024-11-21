package com.vanatta.helene.supplies.database.supplies;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteSupplyRequest {
  // how many different item statuses are there in total.
  public static final int ITEM_STATUS_COUNT = 3;
  // how many different site types in total
  public static final int SITE_TYPE_COUNT = 2;
  @Builder.Default List<String> sites = new ArrayList<>();
  @Builder.Default List<String> items = new ArrayList<>();
  @Builder.Default List<String> counties = new ArrayList<>();
  @Builder.Default List<String> itemStatus = new ArrayList<>();
  @Builder.Default List<String> siteType = new ArrayList<>();
  @Builder.Default Boolean acceptingDonations = true;
  @Builder.Default Boolean notAcceptingDonations = true;
}
