package com.vanatta.helene.supplies.database.supplies;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class SuppliesDao {

  @NoArgsConstructor
  @Data
  public static class SuppliesQueryResult {

    Long siteId;
    String site;
    String county;
    String item;
    String itemStatus;
  }

  static List<SuppliesQueryResult> getSupplyResults(Jdbi jdbi, SiteSupplyRequest request) {
    StringBuilder query =
        new StringBuilder(
            """
      select
        s.id siteId,
        s.name site,
        c.name county,
        i.name item,
        ist.name itemStatus
      from site s
      join county c on c.id = s.county_id
      join site_item si on si.site_id = s.id
      join item i on i.id = si.item_id
      join item_status ist on ist.id = si.item_status_id
      where 1=1
      """);

    if (!request.getSites().isEmpty()) {
      query.append("and s.name in (<sites>)");
    }
    if (!request.getCounties().isEmpty()) {
      query.append("and c.name in (<counties>)");
    }
    if (!request.getItems().isEmpty()) {
      query.append("and i.name in (<items>)");
    }

    if(!request.getItemStatus().isEmpty()) {
      query.append("and ist.name in (<item_status>)");
    }
    query.append("order by c.name, s.name, ist.sort_order, i.name");

    return jdbi.withHandle(
        handle -> {
          var queryBuilder = handle.createQuery(query.toString());
          if (!request.getSites().isEmpty()) {
            queryBuilder.bindList("sites", request.getSites());
          }
          if (!request.getCounties().isEmpty()) {
            queryBuilder.bindList("counties", request.getCounties());
          }
          if (!request.getItems().isEmpty()) {
            queryBuilder.bindList("items", request.getItems());
          }
          if(!request.getItemStatus().isEmpty()) {
            queryBuilder.bindList("item_status", request.getItemStatus());
          }

          return queryBuilder.mapToBean(SuppliesQueryResult.class).list();
        });
  }
}
