package com.vanatta.helene.supplies.database.util;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HtmlSelectOptionsUtil {
  public static List<ItemListing> createItemListing(
      String selectedValue, Collection<String> allValues) {
    return allValues.stream()
        .map(
            value ->
                ItemListing.builder()
                    .name(value)
                    .selected(value.equals(selectedValue) ? "selected" : "")
                    .build())
        .sorted(Comparator.comparing(ItemListing::getName))
        .toList();
  }
  
  @Builder
  @Data
  public static class ItemListing {
    String name;
    
    /** Should either be blank, or "selected" */
    String selected;
  }
}
