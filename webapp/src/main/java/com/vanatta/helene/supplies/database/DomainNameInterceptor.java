package com.vanatta.helene.supplies.database;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Ensure request is by appropriate domain name, otherwise issue a redirect. */
@Configuration
public class DomainNameInterceptor implements WebMvcConfigurer {

  private final List<String> validDomains;

  public DomainNameInterceptor(Jdbi jdbi) {
    validDomains = fetchValidDomains(jdbi);
  }

  static List<String> fetchValidDomains(Jdbi jdbi) {
    List<String> domains = new ArrayList<>();
    domains.add("localhost");
    domains.addAll(
        jdbi.withHandle(
            h ->
                h.createQuery("select distinct domain from deployment")
                    .mapTo(String.class)
                    .list()));
    return domains;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new DomainInterceptor(validDomains));
  }

  @AllArgsConstructor
  static class DomainInterceptor implements HandlerInterceptor {

    List<String> validDomains;

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

      String host = request.getHeader("host");

      if (validDomains.stream().anyMatch(host::contains)) {
        return true;
      } else {
        response.sendRedirect("https://wnc-supply-sites.com");
        return false;
      }
    }
  }
}
