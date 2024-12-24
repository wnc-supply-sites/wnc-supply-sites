package com.vanatta.helene.supplies.database.supplies.filters;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.data.CountyDao;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FilterData are the controls which allow for filtering by site/item/county. These are relatively
 * dynamic data that come from database.
 */
@Slf4j
@RestController
@AllArgsConstructor
public class FilterDataController {
  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;

  @CrossOrigin
  @GetMapping(value = "/supplies/filter-data")
  public FilterDataResponse getFilterData(HttpServletRequest request) {
    AuthenticatedMode authenticatedMode =
        cookieAuthenticator.isAuthenticatedWithUniversalPassword(request)
            ? AuthenticatedMode.AUTHENTICATED
            : AuthenticatedMode.NOT_AUTHENTICATED;
    return getFilterData(authenticatedMode);
  }

  // @VisibleForTesting
  FilterDataResponse getFilterData() {
    return getFilterData(AuthenticatedMode.NOT_AUTHENTICATED);
  }

  // @VisibleForTesting
  FilterDataResponse getFilterData(AuthenticatedMode authenticatedMode) {
    return FilterDataResponse.builder()
        .sites(FilterDataDao.getAllActiveSites(jdbi, authenticatedMode))
        .counties(CountyDao.fetchActiveCountyList(jdbi, authenticatedMode))
        .items(FilterDataDao.getAllItems(jdbi))
        .build();
  }
}
