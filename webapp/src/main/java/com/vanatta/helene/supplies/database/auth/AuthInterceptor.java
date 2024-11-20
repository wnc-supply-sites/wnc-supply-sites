package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.AllArgsConstructor;
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
  private final AuthKey authKey;

  public AuthInterceptor(@Value("${auth.enabled}") String authEnabled, AuthKey authKey) {
    this.authEnabled = Boolean.parseBoolean(authEnabled);
    this.authKey = authKey;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (authEnabled) {
      registry.addInterceptor(new AuthIntercept(authKey));
    }
  }

  @AllArgsConstructor
  static class AuthIntercept implements HandlerInterceptor {
    private final AuthKey authKey;

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

      String requestUri = request.getRequestURI();
      if (!requestUri.startsWith("/manage/")) {
        return true;
      } else {

        String queryString = request.getQueryString();
        if (queryString != null) {
          requestUri += URLEncoder.encode("?" + queryString, StandardCharsets.UTF_8);
        }

        // /manage requested, check auth cookie is present
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
          response.sendRedirect("/login?redirectUri=" + requestUri);
          return false;
        }

        Cookie authCookie =
            Arrays.stream(cookies).filter(c -> c.getName().equals("auth")).findAny().orElse(null);

        if (authCookie == null) {
          response.sendRedirect("/login?redirectUri=" + requestUri);
          return false;
        } else {
          if (authKey.getAuthKey().equals(authCookie.getValue())) {
            return true;
          } else {
            // auth failed, wrong value in cookie. Delete the cookie
            Cookie cookie = new Cookie("auth", null);
            cookie.setMaxAge(0);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            response.sendRedirect("/login?redirectUri=" + requestUri);
            return false;
          }
        }
      }
    }
  }
}
