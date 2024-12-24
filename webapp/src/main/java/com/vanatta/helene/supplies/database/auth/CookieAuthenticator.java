package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stores & generates valid auth keys, auth key needs to be present as a cookie value, and is
 * inspected when accessing /manage URLs to validate user is logged in.
 */
@Component
public class CookieAuthenticator {

  /** AuthKey value is cached. */
  @Getter private final String authKey;

  private final Jdbi jdbi;
  private final boolean allowUniversalLogin;

  @Autowired
  public CookieAuthenticator(Jdbi jdbi, @Value("${allow.universal.login}") boolean allowUniversalLogin) {
    authKey = LoginDao.getAuthKeyOrGenerateIt(jdbi);
    this.jdbi = jdbi;
    this.allowUniversalLogin = allowUniversalLogin;
  }

  public boolean isAuthenticated(HttpServletRequest request) {
    if(allowUniversalLogin && isAuthenticatedWithUniversalPassword(request)) {
      return true;
    }
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
