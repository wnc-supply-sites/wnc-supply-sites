package com.vanatta.helene.supplies.database.auth;

import java.util.UUID;

/**
 * Stores & generates valid auth keys, auth key needs to be present
 * as a cookie value, and is inspected when accessing /manage URLs
 * to validate user is logged in.
 */
public class AuthKey {
  public static final String AUTH_KEY = UUID.randomUUID().toString();
}

