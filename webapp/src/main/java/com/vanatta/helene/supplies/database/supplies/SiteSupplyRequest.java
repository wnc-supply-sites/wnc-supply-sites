package com.vanatta.helene.supplies.database.supplies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteSupplyRequest {
  List<String> sites;
  List<String> items;
  List<String> counties;
  List<String> itemStatus;

}
