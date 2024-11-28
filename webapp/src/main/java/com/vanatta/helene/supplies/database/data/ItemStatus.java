package com.vanatta.helene.supplies.database.data;

import java.util.Arrays;
import java.util.List;

import com.vanatta.helene.supplies.database.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemStatus {
  URGENTLY_NEEDED("Urgently Needed", "urgent"),
  NEEDED("Needed", "needed"),
  AVAILABLE("Available", "available"),
  OVERSUPPLY("Oversupply", "oversupply"),
  ;
  private final String text;
  private final String cssClass;

  public static List<String> allItemStatus() {
    return Arrays.stream(values()).map(s -> s.text).toList();
  }

  /** Converts human readable status name to CSS class name. */
  public static String convertToDisplayClass(String itemStatusText) {
    return EnumUtil.mapText(values(), ItemStatus::getText, itemStatusText)
        .orElseThrow(
            () -> new IllegalArgumentException("Invalid item status text: " + itemStatusText))
        .cssClass;
  }

  public static ItemStatus fromTextValue(String textValue) {
    return EnumUtil.mapText(values(), ItemStatus::getText, textValue)
        .orElseThrow(() -> new IllegalArgumentException("Invalid item status text: " + textValue));
  }
}
