package com.vanatta.helene.supplies.database.supplies;

import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteItem;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteSupplyData;
import com.vanatta.helene.supplies.database.filters.FilterDataResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
public class SuppliesController {

  private final Jdbi jdbi;


  @CrossOrigin
  @PostMapping(value = "/supplies")
  public SiteSupplyResponse getSuppliesData(@RequestBody SiteSupplyRequest request) {
    log.info("Request received: {}", request);

    var results = SuppliesDao.getSupplyResults(jdbi, request);

    Map<Long, SiteSupplyData> aggregatedResults = new HashMap<>();

    results.forEach(
        result ->
            aggregatedResults
                .computeIfAbsent(
                    result.getSiteId(),
                    r ->
                        SiteSupplyData.builder()
                            .site(result.getSite())
                            .county(result.getCounty())
                            .acceptingDonations(result.isAcceptingDonations())
                            .build())
                .getItems()
                .add(
                    SiteItem.builder()
                        .name(result.getItem())
                        .status(result.getItemStatus())
                        .build()));
    List<SiteSupplyData> resultData =
        aggregatedResults.values().stream() //
            .sorted(
                Comparator.comparing(SiteSupplyData::getCounty)
                    .thenComparing(SiteSupplyData::getSite))
            .toList();

    return SiteSupplyResponse.builder() //
        .resultCount(resultData.size())
        .results(resultData)
        .build();
  }
}
