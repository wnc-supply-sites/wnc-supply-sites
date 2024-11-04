package com.vanatta.helene.supply.loader;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplyData {

  @CsvBindByName(column = "Site", required = true)
  String siteName;

  @CsvBindByName(column = "Items", required = true)
  String items;
}
