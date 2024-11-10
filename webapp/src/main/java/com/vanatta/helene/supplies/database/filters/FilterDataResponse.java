package com.vanatta.helene.supplies.database.filters;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FilterDataResponse {
  List<String> sites;
  List<String> counties;
  List<String> items;
}
