package com.vanatta.helene.supplies.database.filters;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class FilterDataController {
  private static List<String> cachedCountiesList;
  private final Jdbi jdbi;

  FilterDataController(Jdbi jdbi) {
    this.jdbi = jdbi;
    if(cachedCountiesList == null) {
      cachedCountiesList = FilterDataDao.getAllCounties(jdbi);
    }
  }

  @CrossOrigin
  @GetMapping(value = "/filter-data")
  public FilterDataResponse getFilterData() {
    return FilterDataResponse.builder()
        .sites(FilterDataDao.getAllSites(jdbi))
        .counties(cachedCountiesList)
        .items(FilterDataDao.getAllItems(jdbi))
        .build();
  }


}
