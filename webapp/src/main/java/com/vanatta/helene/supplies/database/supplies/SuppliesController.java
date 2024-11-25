package com.vanatta.helene.supplies.database.supplies;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteItem;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteSupplyData;
import java.time.format.DateTimeFormatter;
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

  @GetMapping("/supplies/needs")
  public ModelAndView needs() {
    return supplies("donate");
  }

  /** GET requests should be coming from the home page. */
  @GetMapping("/supplies/site-list")
  public ModelAndView supplies(@RequestParam(required = false) String mode) {
    if (mode == null) {
      mode = "view";
    }
    Map<String, String> templateValues = new HashMap<>();
    templateValues.put(
        "notAcceptingDonationsChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");
    templateValues.put("overSupplyChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");
    templateValues.put("availableChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");

    return new ModelAndView("supplies/supplies", templateValues);
  }

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MMM-d");

  /** POST requests should be coming from supplies page JS requests for donation site data */
  @CrossOrigin
  @PostMapping(value = "/supplies/site-data")
  public SiteSupplyResponse getSuppliesData(@RequestBody SiteSupplyRequest request) {
    var results = SuppliesDao.getSupplyResults(jdbi, request);

    Map<Long, SiteSupplyData> aggregatedResults = new HashMap<>();

    results.forEach(
        result -> {
          var siteSupplyData =
              aggregatedResults.computeIfAbsent(
                  result.getSiteId(),
                  _ ->
                      SiteSupplyData.builder()
                          .id(result.getSiteId())
                          .site(result.getSite())
                          .siteType(result.getSiteType())
                          .county(result.getCounty())
                          .acceptingDonations(result.isAcceptingDonations())
                          .lastUpdated(result.getLastUpdated().format(dateTimeFormatter))
                          .build());
          if (result.getItem() != null) {
            siteSupplyData
                .getItems()
                .add(
                    SiteItem.builder()
                        .name(result.getItem())
                        .displayClass(ItemStatus.convertToDisplayClass(result.getItemStatus()))
                        .build());
          }
        });
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
