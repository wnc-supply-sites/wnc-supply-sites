package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

import java.util.Arrays;

/**
 * Stores & generates valid auth keys, auth key needs to be present
 * as a cookie value, and is inspected when accessing /manage URLs
 * to validate user is logged in.
 */
public class CookieAuthenticator {

  /**
   * AuthKey value is cached.
   */
  @Getter
  private final String authKey;

  public CookieAuthenticator(Jdbi jdbi) {
    authKey = LoginDao.getAuthKeyOrGenerateIt(jdbi);
  }

  public boolean isAuthenticated(HttpServletRequest request) {
    // check auth cookie is present
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return false;
    }

    Cookie authCookie =
        Arrays.stream(cookies).filter(c -> c.getName().equals("auth")).findAny().orElse(null);

    if (authCookie == null) {
      return false;
    } else {
      if (authKey.equals(authCookie.getValue())) {
        return true;
      } else {
        return false;
      }
    }
  }



}

