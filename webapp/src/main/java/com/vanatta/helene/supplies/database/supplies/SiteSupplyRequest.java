package com.vanatta.helene.supplies.database.supplies;

import lombok.Data;

import java.util.List;

@Data
public class SiteSupplyRequest {
  List<String> sites;
  List<String> items;
  List<String> counties;
  List<String> itemStatus;

}
