package com.vanatta.helene.supplies.database.auth;

import com.vanatta.helene.supplies.database.util.CookieUtil;
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
  private final String universalUser;
  private final String universalPassword;
  private final boolean allowUniversalLogin;

  LoginController(
      Jdbi jdbi,
      @Value("${auth.user}") String user,
      @Value("${auth.pass}") String pass,
      @Value("${allow.universal.login}") boolean allowUniversalLogin) {
    this.jdbi = jdbi;
    this.universalUser = user;
    this.universalPassword = pass;
    this.allowUniversalLogin = allowUniversalLogin;
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

    if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
      Map<String, String> pageParams = new HashMap<>();
      pageParams.put("redirectUri", redirectUri);
      pageParams.put("errorMessage", "Invalid Login");
      return new ModelAndView("login/login", pageParams);
    } else if (PasswordDao.confirmPassword(jdbi, user, password)) {
      LoginDao.recordLoginSuccess(jdbi, user);
      String authToken = LoginDao.generateAuthToken(jdbi, user);
      CookieUtil.setCookie(response, "auth", authToken);
      return new ModelAndView("redirect:" + redirectUri);
    } else if (universalUser.equalsIgnoreCase(user.trim())
        && universalPassword.equalsIgnoreCase(password.trim())) {
      if (allowUniversalLogin) {
        LoginDao.recordLoginSuccess(jdbi, user);
        String authToken = LoginDao.getAuthKeyOrGenerateIt(jdbi);
        CookieUtil.setCookie(response, "auth", authToken);
        return new ModelAndView("redirect:" + redirectUri);
      } else {
        return new ModelAndView("redirect:/login/setup-password");
      }
    } else {
      LoginDao.recordLoginFailure(jdbi, user);
      log.info("User login failed: {}, IP: {}", user, request.getRemoteAddr());
      Map<String, String> pageParams = new HashMap<>();
      pageParams.put("redirectUri", redirectUri);
      pageParams.put("errorMessage", "Invalid Login");
      return new ModelAndView("login/login", pageParams);
    }
  }
}
