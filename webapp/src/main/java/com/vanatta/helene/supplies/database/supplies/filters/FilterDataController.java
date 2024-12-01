package com.vanatta.helene.supplies.database.supplies.filters;

import com.vanatta.helene.supplies.database.data.CountyDao;
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

  @CrossOrigin
  @GetMapping(value = "/supplies/filter-data")
  public FilterDataResponse getFilterData() {
    return FilterDataResponse.builder()
        .sites(FilterDataDao.getAllActiveSites(jdbi))
        .counties(CountyDao.fetchActiveCountyList(jdbi))
        .items(FilterDataDao.getAllItems(jdbi))
        .build();
  }
}
