package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class LoggedInAdvice {

  private final CookieAuthenticator cookieAuthenticator;
  
  
  @ModelAttribute("loggedIn")
  public boolean loggedIn(HttpServletRequest request) {
    return cookieAuthenticator.isAuthenticated(request);
  }
}
