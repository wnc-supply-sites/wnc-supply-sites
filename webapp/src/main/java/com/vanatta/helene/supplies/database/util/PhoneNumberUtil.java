package com.vanatta.helene.supplies.database.util;

public class PhoneNumberUtil {

  public static String removeNonNumeric(String input) {
    return input.replaceAll("[^\\d]", "");
  }

  /**
   * Checks if a given phone number looks valid. Phone numbers with country code are not considered
   * valid. Phone numbers look valid if they contain exactly 10 digits.
   */
  public static boolean isValid(String input) {
    return input != null && removeNonNumeric(input).length() == 10;
  }
}
