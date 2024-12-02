package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

/** Checks HTTP headers for auth-secret value */
@AllArgsConstructor
class WebhookAuthenticator {

  private final String secretValue;

  boolean hasCorrectSecret(HttpServletRequest request) {
    return secretValue.equals(request.getHeader("WebhookSecret"));
  }
}
