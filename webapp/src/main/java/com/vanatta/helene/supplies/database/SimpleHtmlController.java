package com.vanatta.helene.supplies.database;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.auth.UserRole;
import com.vanatta.helene.supplies.database.data.HostNameLookup;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/** Controller for the various HTML pages that are relatively 'simple' and are mostly static. */
@Controller
@AllArgsConstructor
public class SimpleHtmlController {

  private final Jdbi jdbi;
  private final HostNameLookup hostNameLookup;

  @GetMapping("/")
  public ModelAndView home(
      HttpServletRequest request, @ModelAttribute(LoggedInAdvice.USER_ROLES) List<UserRole> roles) {
    Map<String, Object> params = new HashMap<>();
    params.put("isAuthenticated", roles.contains(UserRole.AUTHORIZED));
    params.put("isDriver", roles.contains(UserRole.DRIVER));
    params.put("canManageSites", UserRole.canManageSites(roles));
    DeploymentDescription deploymentDescription =
        fetchDeploymentDescription(jdbi, hostNameLookup.lookupHostName(request));
    params.put("siteDescription", deploymentDescription.getSiteDescription());
    params.put("contactUsLink", deploymentDescription.getContactUsLink());
    return new ModelAndView("home/home", params);
  }

  @Data
  public static class DeploymentDescription {
    String contactUsLink;
    String siteDescription;
  }

  // @VisibleForTesting
  static DeploymentDescription fetchDeploymentDescription(Jdbi jdbi, String domain) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    "select deployment.contact_us_link, site_description from deployment where domain = :domain")
                .bind("domain", domain)
                .mapToBean(DeploymentDescription.class)
                .one());
  }

  @GetMapping("/log-out")
  public RedirectView logout(HttpServletResponse response) {
    Cookie cookie = new Cookie("auth", null);
    cookie.setMaxAge(0);
    cookie.setSecure(true);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
    return new RedirectView("/");
  }
  
  
  @GetMapping("/about/")
  ModelAndView showAbout() {
    return new ModelAndView("about/about");
  }
  
  @GetMapping("/about/changelog/")
  ModelAndView showChangelog() {
    return new ModelAndView("about/changelog/changelog");
  }
  
  @GetMapping("/about/partners/")
  ModelAndView showPartners() {
    return new ModelAndView("about/partners/patners");
  }
  @GetMapping("/about/thestory/")
  ModelAndView showTheStory() {
    return new ModelAndView("about/thestory/thestory");
  }
  @GetMapping("/behind-the-scenes/")
  ModelAndView showBehindTheScenes() {
    return new ModelAndView("behind-the-scenes/index");
  }
  
  @GetMapping("/registration/")
  ModelAndView showRegistrationPage(HttpServletRequest request) {
    
    DeploymentDescription deploymentDescription =
        fetchDeploymentDescription(jdbi, hostNameLookup.lookupHostName(request));
    Map<String, Object> params = new HashMap<>();
    params.put("contactUsLink", deploymentDescription.getContactUsLink());
    return new ModelAndView("registration/registration", params);
  }
}
