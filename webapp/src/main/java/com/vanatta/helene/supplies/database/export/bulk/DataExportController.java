package com.vanatta.helene.supplies.database.export.bulk;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.filters.FilterDataDao;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for special bulk data export endpoints. These endpoints serve large JSON data payloads
 * useful for grabbing all the data.
 *
 * <p>For sending updates when item status changes, see classes like {@see SendSiteUpdate}
 */
@RestController
@AllArgsConstructor
public class DataExportController {

  private static final Gson gson = new Gson();
  private final Jdbi jdbi;

  @GetMapping("/export/items")
  ResponseEntity<String> itemList() {
    List<String> items = FilterDataDao.getAllItems(jdbi);
    return ResponseEntity.ok("{\"items\":" + gson.toJson(items) + "}");
  }

  @GetMapping("/export/site-list")
  ResponseEntity<String> siteList() {
    List<DataExportDao.SiteExportJson> sites = DataExportDao.fetchAllSites(jdbi);
    return ResponseEntity.ok("{\"sites\":" + gson.toJson(sites) + "}");
  }

  @GetMapping("/export/site-items")
  ResponseEntity<String> siteItems() {
    List<DataExportDao.SiteItemExportJson> sites = DataExportDao.fetchAllSiteItems(jdbi);
    return ResponseEntity.ok("{\"sites-items\":" + gson.toJson(sites) + "}");
  }
}
