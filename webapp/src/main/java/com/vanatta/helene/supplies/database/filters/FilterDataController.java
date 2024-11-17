package com.vanatta.helene.supplies.database.filters;

import java.util.List;
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
public class FilterDataController {
  /**
   * Counties are not expected to change for the next couple years, can be cached in memory without
   * going stale.
   */
  private static List<String> cachedCountiesList;

  private final Jdbi jdbi;

  FilterDataController(Jdbi jdbi) {
    this.jdbi = jdbi;
    if (cachedCountiesList == null) {
      cachedCountiesList = FilterDataDao.getAllCounties(jdbi);
    }
  }

  @CrossOrigin
  @GetMapping(value = "/supplies/filter-data")
  public FilterDataResponse getFilterData() {
    return FilterDataResponse.builder()
        .sites(FilterDataDao.getAllSites(jdbi))
        .counties(cachedCountiesList)
        .items(FilterDataDao.getAllItems(jdbi))
        .build();
  }
}
