package com.vanatta.helene.supplies.database.supplies;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class SuppliesDao {

  @NoArgsConstructor
  @Data
  public static class SuppliesQueryResult {
    Long siteId;
    boolean acceptingDonations;
    String site;
    String siteType;
    String county;
    String item;
    String itemStatus;
    LocalDate lastUpdated;
  }

  static List<SuppliesQueryResult> getSupplyResults(Jdbi jdbi, SiteSupplyRequest request) {
    StringBuilder query =
        new StringBuilder(
            """
      select
        s.id siteId,
        s.accepting_donations acceptingDonations,
        s.name site,
        st.name siteType,
        c.name county,
        i.name item,
        ist.name itemStatus,
        s.last_updated lastUpdated
      from site s
      join site_type st on st.id = s.site_type_id
      join county c on c.id = s.county_id
      left join site_item si on si.site_id = s.id
      left join item i on i.id = si.item_id
      left join item_status ist on ist.id = si.item_status_id
      where s.active = true
      """);

    if (!request.getSites().isEmpty()) {
      query.append("and s.name in (<sites>)\n");
    }
    if (!request.getCounties().isEmpty()) {
      query.append("and c.name in (<counties>)\n");
    }
    if (!request.getItems().isEmpty()) {
      query.append("and i.name in (<items>)\n");
    }

    // if item status length is 3, then we are asking for all item status
    // but, if we do that, we filter out sites with no item status.
    // If all item statuses are requested, then we treat it as if none are requested.
    if (!request.getItemStatus().isEmpty()
        && request.getItemStatus().size() < SiteSupplyRequest.ITEM_STATUS_COUNT) {
      query.append("and ist.name in (<item_status>)\n");
    }

    if (!request.getSiteType().isEmpty()
        && request.getSiteType().size() < SiteSupplyRequest.SITE_TYPE_COUNT) {
      query.append("and st.name in (<site_type>)\n");
    }

    if (request.getAcceptingDonations() != request.getNotAcceptingDonations()) {
      query
          .append("and accepting_donations = ")
          .append(request.getAcceptingDonations())
          .append("\n");
    }

    query.append("\norder by c.name, s.name, ist.sort_order, i.name");

    List<String> decodedSites =
        request.getSites().stream().map(s -> s.replace("&amp;", "&")).toList();

    return jdbi.withHandle(
        handle -> {
          var queryBuilder = handle.createQuery(query.toString());
          if (!request.getSites().isEmpty()) {
            queryBuilder.bindList("sites", decodedSites);
          }
          if (!request.getCounties().isEmpty()) {
            queryBuilder.bindList("counties", request.getCounties());
          }
          if (!request.getItems().isEmpty()) {
            queryBuilder.bindList("items", request.getItems());
          }
          if (!request.getItemStatus().isEmpty()
              && request.getItemStatus().size() < SiteSupplyRequest.ITEM_STATUS_COUNT) {
            queryBuilder.bindList("item_status", request.getItemStatus());
          }

          if (!request.getSiteType().isEmpty()
              && request.getSiteType().size() < SiteSupplyRequest.SITE_TYPE_COUNT) {
            queryBuilder.bindList("site_type", request.getSiteType());
          }

          return queryBuilder.mapToBean(SuppliesQueryResult.class).list();
        });
  }
}
