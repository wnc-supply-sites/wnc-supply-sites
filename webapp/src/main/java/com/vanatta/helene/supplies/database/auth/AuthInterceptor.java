package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Intercepts requests, checks if user is accessing /manage URL, if so, checks 'auth' cookie for
 * valid auth.
 */
@Configuration
public class AuthInterceptor implements WebMvcConfigurer {

  private final boolean authEnabled;
  private final CookieAuthenticator cookieAuthenticator;
  private final WebhookAuthenticator webhookAuthenticator;

  public AuthInterceptor(
      @Value("${auth.enabled}") String authEnabled,
      @Value("${webhook.auth.secret}") String webhookAuthSecret,
      Jdbi jdbi) {
    this.authEnabled = Boolean.parseBoolean(authEnabled);
    this.cookieAuthenticator = new CookieAuthenticator(jdbi);
    this.webhookAuthenticator = new WebhookAuthenticator(webhookAuthSecret);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (authEnabled) {
      registry.addInterceptor(new AuthIntercept(cookieAuthenticator, webhookAuthenticator));
    }
  }

  @AllArgsConstructor
  static class AuthIntercept implements HandlerInterceptor {
    private final CookieAuthenticator cookieAuthenticator;
    private final WebhookAuthenticator webhookAuthenticator;

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

      String requestUri = request.getRequestURI();
      if (requestUri.startsWith("/manage/")) {

        String queryString = request.getQueryString();
        if (queryString != null) {
          requestUri += URLEncoder.encode("?" + queryString, StandardCharsets.UTF_8);
        }

        if (cookieAuthenticator.isAuthenticated(request)) {
          return true;
        } else {
          // auth failed, delete cookie if present
          Cookie cookie = new Cookie("auth", null);
          cookie.setMaxAge(0);
          cookie.setSecure(true);
          cookie.setHttpOnly(true);
          response.addCookie(cookie);
          response.sendRedirect("/login?redirectUri=" + requestUri);
          return false;
        }
      } else if (requestUri.startsWith("/import/")) {
        if (webhookAuthenticator.hasCorrectSecret(request)) {
          return true;
        } else {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          return false;
        }
      } else {
        return true;
      }
    }
  }
}
