package com.vanatta.helene.supplies.database.util;

import org.apache.commons.codec.digest.DigestUtils;

public class HashingUtil {

  public static String sha256(String input) {
    return DigestUtils.sha256Hex(input);
  }
}
