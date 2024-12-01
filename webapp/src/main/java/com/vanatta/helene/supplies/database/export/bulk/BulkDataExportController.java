package com.vanatta.helene.supplies.database.export.bulk;

import com.google.gson.Gson;
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
public class BulkDataExportController {

  private static final Gson gson = new Gson();
  private final Jdbi jdbi;

  @GetMapping("/export/data")
  ResponseEntity<String> exportData() {
    List<String> items = BulkDataExportDao.getAllItems(jdbi);
    List<BulkDataExportDao.SiteExportJson> sites = BulkDataExportDao.fetchAllSites(jdbi);

    return ResponseEntity.ok(
        String.format(
            """
       {
          "items":%s,
          "sites":%s,
          "needRequests":%s
        }
    """,
            gson.toJson(items), gson.toJson(sites), gson.toJson()));
  }
}
