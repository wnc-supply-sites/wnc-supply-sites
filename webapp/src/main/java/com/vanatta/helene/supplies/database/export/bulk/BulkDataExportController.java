package com.vanatta.helene.supplies.database.export.bulk;

import com.google.gson.Gson;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

  private final Jdbi jdbi;

  @GetMapping("/export/data")
  ResponseEntity<ExportDataJson> exportData() {
    List<String> items = BulkDataExportDao.getAllItems(jdbi);
    List<BulkDataExportDao.SiteExportJson> sites = BulkDataExportDao.fetchAllSites(jdbi);

    return ResponseEntity.ok(
        ExportDataJson.builder().items(items).sites(sites).needRequests(List.of()).build());
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExportDataJson {
    List<String> items;
    List<BulkDataExportDao.SiteExportJson> sites;
    List<String> needRequests;
  }
}
