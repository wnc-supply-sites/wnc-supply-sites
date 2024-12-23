package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class LoginController {

  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;
  private final String validUser;
  private final String validPass;

  LoginController(
      Jdbi jdbi, @Value("${auth.user}") String user, @Value("${auth.pass}") String pass) {
    this.jdbi = jdbi;
    this.cookieAuthenticator = new CookieAuthenticator(jdbi);
    this.validUser = user;
    this.validPass = pass;
  }

  @GetMapping("/login/login")
  public ModelAndView login(@RequestParam(required = false) String redirectUri) {
    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("redirectUri", Optional.ofNullable(redirectUri).orElse("/manage/select-site"));
    pageParams.put("errorMessage", "");
    return new ModelAndView("login/login", pageParams);
  }

  @GetMapping("/login/setup-password")
  public ModelAndView passwordSetup(@RequestParam(required = false) String redirectUri) {
    Map<String, String> pageParams = new HashMap<>();
    return new ModelAndView("login/setup-password", pageParams);
  }

  @PostMapping(
      path = "/doLogin",
      consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  public ModelAndView doLogin(
      @RequestParam MultiValueMap<String, String> params,
      HttpServletRequest request,
      HttpServletResponse response) {
    String user = params.get("user").getFirst();
    String password = params.get("password").getFirst();
    String redirectUri = params.get("redirectUri").getFirst();

    if (user != null
        && validUser.equalsIgnoreCase(user.trim())
        && password != null
        && validPass.equalsIgnoreCase(password.trim())) {
      LoginDao.recordLoginSuccess(jdbi, request.getRemoteAddr());
      Cookie cookie = new Cookie("auth", cookieAuthenticator.getAuthKey());
      response.addCookie(cookie);
      cookie.setMaxAge(14 * 24 * 60 * 60); // expires in 14 days
      cookie.setSecure(true);
      cookie.setHttpOnly(true);
      return new ModelAndView("redirect:" + redirectUri);
    } else {
      // TODO: logging password is sketchy. When we move to unique logins, do not log the password
      // attempted.
      log.warn(
          "Failed login, user: {}, password: {}, IP: {}", user, password, request.getRemoteAddr());
      LoginDao.recordLoginFailure(jdbi, request.getRemoteAddr());
      Map<String, String> pageParams = new HashMap<>();
      pageParams.put("redirectUri", redirectUri);
      pageParams.put("errorMessage", "Invalid Login");
      return new ModelAndView("login", pageParams);
    }
  }
}
