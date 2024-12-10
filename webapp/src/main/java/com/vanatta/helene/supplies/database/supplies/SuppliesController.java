package com.vanatta.helene.supplies.database.supplies;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteItem;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteSupplyData;
import com.vanatta.helene.supplies.database.supplies.SuppliesDao.SupplyDataCsvBean;
import de.siegmar.fastcsv.writer.CsvWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
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
  public static final String PATH_SUPPLY_SEARCH = "/supplies/site-list";

  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;

  @GetMapping("/supplies/needs")
  public ModelAndView needs(HttpServletRequest request) {
    return supplies("donate", request);
  }

  /** GET requests should be coming from the home page. */
  @GetMapping(PATH_SUPPLY_SEARCH)
  public ModelAndView supplies(
      @RequestParam(required = false) String mode, HttpServletRequest request) {
    if (mode == null) {
      mode = "view";
    }

    Map<String, Object> templateValues = new HashMap<>();
    templateValues.put(
        "notAcceptingDonationsChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");
    templateValues.put("overSupplyChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");
    templateValues.put("availableChecked", mode.equalsIgnoreCase("donate") ? "" : "checked");

    templateValues.put("loggedIn", cookieAuthenticator.isAuthenticated(request));
    return new ModelAndView("supplies/supplies", templateValues);
  }

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MMM-d");

  @GetMapping(value = "/supplies/all-data-json")
  public SiteSupplyResponse getSuppliesData() {
    return getSuppliesData(SiteSupplyRequest.builder().build());
  }

  /**
   * POST requests should be coming from supplies page JS requests for donation site data
   *
   * <p>Returns a JSON object that lists sites and their supply inventory levels filtered by the
   * incoming request.
   */
  @CrossOrigin
  @PostMapping(value = "/supplies/site-data")
  public SiteSupplyResponse getSuppliesData(
      HttpServletRequest httpRequest, @RequestBody SiteSupplyRequest request) {
    boolean authenticated = cookieAuthenticator.isAuthenticated(httpRequest);
    return getSuppliesData(request, authenticated);
  }

  // @VisibleForTesting
  SiteSupplyResponse getSuppliesData(SiteSupplyRequest request) {
    return getSuppliesData(request, false);
  }

  // @VisibleForTesting
  SiteSupplyResponse getSuppliesData(SiteSupplyRequest request, boolean isAuthenticated) {
    request = request.toBuilder().isAuthenticatedUser(isAuthenticated).build();

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
                          .inventoryLastUpdated(
                              result.getInventoryLastUpdated().format(dateTimeFormatter))
                          .build());
          // add items to the corresponding needed or available lists
          if (result.getItem() != null) {
            var itemStatus = ItemStatus.fromTextValue(result.getItemStatus());
            var item =
                SiteItem.builder()
                    .name(result.getItem())
                    .displayClass(itemStatus.getCssClass())
                    .build();
            if (itemStatus == ItemStatus.AVAILABLE || itemStatus == ItemStatus.OVERSUPPLY) {
              siteSupplyData.getAvailableItems().add(item);
            } else {
              siteSupplyData.getNeededItems().add(item);
            }
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

  @GetMapping("/supplies/download")
  void downloadCsv(HttpServletResponse response) throws Exception {
    response.setContentType("text/plain; charset=utf-8");
    response.getWriter().print(generateCsv(jdbi));
  }

  static String generateCsv(Jdbi jdbi) throws IOException {

    List<SupplyDataCsvBean> data = SuppliesDao.fetchCsvData(jdbi);

    StringWriter writer = new StringWriter();
    try (CsvWriter csv = CsvWriter.builder().build(writer)) {
      csv.writeRecord(
          "Site Id", "Site Name", "County", "Item Id", "Item Name", "Item Status", "Last Updated");

      data.forEach(
          value ->
              csv.writeRecord(
                  String.valueOf(value.getSiteId()),
                  value.getSiteName(),
                  value.getCounty(),
                  String.valueOf(value.getItemId()),
                  value.getItemName(),
                  value.getItemStatus(),
                  value.getLastUpdated()));

      return writer.toString();
    }
  }
}
