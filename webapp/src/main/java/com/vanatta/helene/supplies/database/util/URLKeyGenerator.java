package com.vanatta.helene.supplies.database.util;
import java.security.SecureRandom;
import java.math.BigInteger;

public class URLKeyGenerator {
  private static final SecureRandom random = new SecureRandom();

  public static String generateUrlKey() {
    String key = new BigInteger(20, random).toString(16).toUpperCase(); // Generate up to 5 hex characters
    return key.length() >= 4 ? key.substring(0, 4) : String.format("%4s", key).replace(' ', '0'); // Ensure 4 characters
  }

  public static void main(String[] args) {
    System.out.println("Generated URL Key: " + generateUrlKey());
  }
}

