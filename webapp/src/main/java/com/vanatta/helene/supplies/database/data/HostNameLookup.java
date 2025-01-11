package com.vanatta.helene.supplies.database.data;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility to look up the current hostname from the current request. Allows for hostname lookup to
 * have an override so we can simulate different domains from a development context.
 */
@Component
public class HostNameLookup {
  private final String defaultDeployment;

  HostNameLookup(@Value("${dev.default.deployment}") String deployment) {
    this.defaultDeployment = deployment;
  }

  public String lookupHostName(HttpServletRequest request) {
    if (defaultDeployment != null && !defaultDeployment.isEmpty()) {
      return defaultDeployment;
    } else {
      return request.getHeader("host");
    }
  }
}
