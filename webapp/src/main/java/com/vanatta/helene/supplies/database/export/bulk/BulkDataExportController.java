package com.vanatta.helene.supplies.database.export.bulk;

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
    List<BulkDataExportDao.NeedRequest> needRequests = BulkDataExportDao.getAllNeedsRequests(jdbi);

    return ResponseEntity.ok(
        ExportDataJson.builder() //
            .items(items)
            .sites(sites)
            .needRequests(needRequests)
            .build());
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExportDataJson {
    List<String> items;
    List<BulkDataExportDao.SiteExportJson> sites;
    List<BulkDataExportDao.NeedRequest> needRequests;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class NeedsResponseJson {
    private List<NeedsDbResult> needs;
  }

  @Data
  public static class NeedsDbResult {
    long wssId;
    String siteName;
    String needPriority;
    String itemName;
  }

  @GetMapping("/export/needs")
  ResponseEntity<NeedsResponseJson> exportNeeds() {

    List<NeedsDbResult> needs = fetchAllNeeds(jdbi);
    return ResponseEntity.ok(NeedsResponseJson.builder().needs(needs).build());
  }

  public static List<NeedsDbResult> fetchAllNeeds(Jdbi jdbi) {
    String query =
        """
          select
            si.wss_id wssId,
            s.name siteName,
            ist.name needPriority,
            i.name itemName
          from site s
          join site_item si on si.site_id = s.id
          join item i on i.id = si.item_id
          join item_status ist on ist.id = si.item_status_id
        """;

    return jdbi.withHandle(
        handle -> handle.createQuery(query).mapToBean(NeedsDbResult.class).list());
  }
}
