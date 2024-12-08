package com.vanatta.helene.supplies.database.supplies.site.details;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class NeedsMatchingDao {

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

  public static List<NeedsMatchingResult> execute(Jdbi jdbi, long airtableId) {
    String query = "select id from site where airtable_id = :airtableId";
    long dbId =
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(query)
                        .bind("airtableId", airtableId)
                        .mapTo(Long.class)
                        .findOne())
            .orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + airtableId));
    return executeByInternalId(jdbi, dbId);
  }

  public static List<NeedsMatchingResult> executeByInternalId(Jdbi jdbi, long dbId) {
    String query =
        """
         WITH needy_items AS (
            SELECT si.item_id
            FROM site_item si
            JOIN item_status ist ON si.item_status_id = ist.id
            JOIN site s on s.id = si.site_id
            WHERE s.id = :id AND ist.name IN ('Urgently Needed', 'Needed')
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
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("id", dbId)
                .mapToBean(NeedsMatchingResult.class)
                .list());
  }
}
