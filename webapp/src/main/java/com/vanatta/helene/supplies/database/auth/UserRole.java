package com.vanatta.helene.supplies.database.auth;

import java.util.List;

public enum UserRole {
  AUTHORIZED,
  DRIVER,
  DISPATCHER,
  SITE_MANAGER,
  DATA_ADMIN,
  ;

  static boolean hasGodMode(List<UserRole> userRoles) {
    return userRoles.contains(DISPATCHER) || userRoles.contains(DATA_ADMIN);
  }
}
