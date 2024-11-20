package com.vanatta.helene.supplies.database.auth;

import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

/**
 * Stores & generates valid auth keys, auth key needs to be present
 * as a cookie value, and is inspected when accessing /manage URLs
 * to validate user is logged in.
 */
public class AuthKey {

  /**
   * AuthKey value is cached.
   */
  @Getter
  private final String authKey;

  public AuthKey(Jdbi jdbi) {
    authKey = LoginDao.getAuthKeyOrGenerateIt(jdbi);
  }
}

