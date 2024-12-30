package com.vanatta.helene.supplies.database.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeFormat {

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MMM-d");

  public static String format(LocalDateTime dateTime) {
    return dateTimeFormatter.format(dateTime);
  }
}
