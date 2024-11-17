package com.vanatta.helene.supplies.database.supplies;

import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteItem;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteSupplyData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@AllArgsConstructor
public class SuppliesController {

  private final Jdbi jdbi;

  /** GET requests should be coming from the home page. */
  @GetMapping("/supplies")
  public ModelAndView supplies(@RequestParam String mode) {
    Map<String, String> templateValues = new HashMap<>();
    templateValues.put(
        "notAcceptingDonationsValue", mode.equalsIgnoreCase("donate") ? "" : "checked");
    templateValues.put("overSupplyValue", mode.equalsIgnoreCase("donate") ? "" : "checked");

    return new ModelAndView("supplies", templateValues);
  }

  /** POST requests should be coming from supplies page JS requests for donation site data */
  @CrossOrigin
  @PostMapping(value = "/supplies")
  public SiteSupplyResponse getSuppliesData(@RequestBody SiteSupplyRequest request) {
    var results = SuppliesDao.getSupplyResults(jdbi, request);

    Map<Long, SiteSupplyData> aggregatedResults = new HashMap<>();

    results.forEach(
        result ->
            aggregatedResults
                .computeIfAbsent(
                    result.getSiteId(),
                    _ ->
                        SiteSupplyData.builder()
                            .id(result.getSiteId())
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
