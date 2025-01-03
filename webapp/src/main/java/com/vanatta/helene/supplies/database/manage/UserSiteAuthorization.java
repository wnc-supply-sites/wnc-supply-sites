package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;

public class UserSiteAuthorization {
  /** checks if user is authorized for current site, if so, returns the site name. */
  public static Optional<String> isAuthorizedForSite(
      Jdbi jdbi, List<Long> authorizedSites, String currentSite) {
    if (currentSite == null
        || currentSite.isBlank()
        || PhoneNumberUtil.removeNonNumeric(currentSite).length() != currentSite.length()
        || !authorizedSites.contains(Long.parseLong(currentSite))) {
      return Optional.empty();
    }

    return Optional.ofNullable(ManageSiteDao.fetchSiteName(jdbi, currentSite));
  }
}
