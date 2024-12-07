package com.vanatta.helene.supplies.database.export;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class DataMatchingController {

  private final Jdbi jdbi;

  @GetMapping("/export/needs-matching")
  ModelAndView needsMatching(@RequestParam(required = true) String airtableId) {
    // TODO: validate input & throw bad arg..

    var matchList = execute(jdbi, Long.parseLong(airtableId));

    Map<String, Object> pageData = new HashMap<>();
    pageData.put("matchingResults", matchList);
    return new ModelAndView("export/needs-matching", pageData);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class NeedsMatchingResult {
    long airtableId;
    long wssId;
    String siteName;
    String siteAddress;
    String city;
    String county;
    String state;
    String itemName;
    int overlapCount;
  }

  static List<NeedsMatchingResult> execute(Jdbi jdbi, long airtableId) {
    String query =
        """
         WITH needy_items AS (
            SELECT si.item_id
            FROM site_item si
            JOIN item_status ist ON si.item_status_id = ist.id
            JOIN site s on s.id = si.site_id
            WHERE s.airtable_id = :airtableId AND ist.name IN ('Urgently Needed', 'Needed')
        ),
        oversupply_sites AS (
            SELECT si.site_id, si.item_id
            FROM site_item si
            JOIN item_status ist ON si.item_status_id = ist.id
            WHERE ist.name = 'Oversupply'
        )
        SELECT
            s.airtable_id airtableId,
            s.wss_id AS wssId,
            s.name AS siteName,
            s.address AS siteAddress,
            s.city as city,
            c.name as county,
            c.state as state,
            i.name AS itemName,
            COUNT(os.item_id) OVER (PARTITION BY s.id) AS overlap_count
        FROM
            oversupply_sites os
        JOIN
            needy_items ni ON os.item_id = ni.item_id
        JOIN
            site s ON os.site_id = s.id
        JOIN
            county c on c.id = s.county_id
        JOIN
            item i ON os.item_id = i.id
        ORDER BY
            s.id, overlap_count DESC, i.name;
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query)
        .bind("airtableId", airtableId)
        .mapToBean(NeedsMatchingResult.class)
        .list());
  }
}
