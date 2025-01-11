package com.vanatta.helene.supplies.database;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Configures parameters based upon the requested domain. Different deployments are partitioned
 * by the domain name.
 */

@ControllerAdvice
@Slf4j
public class DeploymentAdvice {
  @ModelAttribute("domain")
  public String domain(HttpServletRequest request) {
    log.info("All headers: {}", List.of(request.getHeaderNames().asIterator()));
    log.info("Host header: {}", request.getHeader("host"));
    return "";
  }
}
