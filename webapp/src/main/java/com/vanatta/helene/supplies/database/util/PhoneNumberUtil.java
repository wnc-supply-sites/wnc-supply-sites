package com.vanatta.helene.supplies.database.util;

public class PhoneNumberUtil {

  // todo test me
  public static String removeNonNumeric(String input) {
    return input.replaceAll("[^\\d]", "");
  }
}
