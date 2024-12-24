package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

/**
 * Stores & generates valid auth keys, auth key needs to be present as a cookie value, and is
 * inspected when accessing /manage URLs to validate user is logged in.
 */
public class CookieAuthenticator {

  /** AuthKey value is cached. */
  @Getter private final String authKey;

  private final Jdbi jdbi;

  public CookieAuthenticator(Jdbi jdbi) {
    authKey = LoginDao.getAuthKeyOrGenerateIt(jdbi);
    this.jdbi = jdbi;
  }

  public boolean isAuthenticated(HttpServletRequest request) {
    return CookieUtil.readCookieValue(request, "auth")
        .map(auth -> LoginDao.isLoggedIn(jdbi, auth))
        .orElse(false);
  }

  public boolean isAuthenticatedWithUniversalPassword(HttpServletRequest request) {
    return CookieUtil.readCookieValue(request, "auth")
        .map(auth -> auth.equals(authKey))
        .orElse(false);
  }
}
