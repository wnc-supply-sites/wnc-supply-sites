package com.vanatta.helene.supplies.database.browse.routes;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.data.SiteAddress;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailController;
import com.vanatta.helene.supplies.database.util.DurationFormatter;
import com.vanatta.helene.supplies.database.util.ListSplitter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.SqlStatement;

public class BrowseRoutesDao {

  @Value
  @Builder
  private static class SitePair {
    long fromSiteId;
    long toSiteId;
  }

  /**
   * Groups up database results, what is many rows, to rows aggregated by site. The difference
   * between rows is the item listing and the item urgency.
   */
  // @VisibleForTesting
  static List<DeliveryOption> aggregate(List<DeliveryOptionDbResult> dbResult) {
    Map<SitePair, DeliveryOption> needsMatchingResult = new HashMap<>();

    dbResult.forEach(
        deliveryOptionDbResult ->
            needsMatchingResult
                .computeIfAbsent(
                    SitePair.builder()
                        .fromSiteId(deliveryOptionDbResult.fromSiteId)
                        .toSiteId(deliveryOptionDbResult.siteId)
                        .build(),
                    _ ->
                        DeliveryOption.builder()
                            .fromSiteName(deliveryOptionDbResult.fromSiteName)
                            .fromSiteLink(
                                SiteDetailController.buildSiteLink(
                                    deliveryOptionDbResult.fromSiteId))
                            .fromSiteWssId(deliveryOptionDbResult.fromSiteWssId)
                            .fromAddress(deliveryOptionDbResult.fromAddress)
                            .fromCity(deliveryOptionDbResult.fromCity)
                            .fromCounty(deliveryOptionDbResult.fromCounty)
                            .fromState(deliveryOptionDbResult.fromState)
                            .fromHours(deliveryOptionDbResult.fromHours)
                            .toSiteName(deliveryOptionDbResult.siteName)
                            .toSiteLink(
                                SiteDetailController.buildSiteLink(deliveryOptionDbResult.siteId))
                            .toSiteWssId(deliveryOptionDbResult.toSiteWssId)
                            .toAddress(deliveryOptionDbResult.siteAddress)
                            .toCity(deliveryOptionDbResult.city)
                            .toCounty(deliveryOptionDbResult.county)
                            .toState(deliveryOptionDbResult.state)
                            .toHours(deliveryOptionDbResult.hours)
                            .driveTimeSeconds(deliveryOptionDbResult.driveTimeSeconds)
                            .distanceMiles(deliveryOptionDbResult.distanceMiles)
                            .build())
                .addItem(
                    DeliveryOption.Item.builder()
                        .name(deliveryOptionDbResult.itemName)
                        .urgencyCssClass(
                            ItemStatus.fromTextValue(deliveryOptionDbResult.urgency).getCssClass())
                        .build()));

    return needsMatchingResult.values().stream()
        .sorted(
            Comparator.comparingInt(DeliveryOption::getItemCount)
                .reversed()
                .thenComparing(DeliveryOption::getToSiteName))
        .toList();
  }

  @Value
  @Builder(toBuilder = true)
  @AllArgsConstructor
  public static class DeliveryOption {
    @Value
    @Builder
    static class Item {
      String name;
      String urgencyCssClass;
    }

    String fromSiteName;
    long fromSiteWssId;
    String fromSiteLink;
    String fromAddress;
    String fromCity;
    String fromCounty;
    String fromState;
    String fromHours;

    String toSiteName;
    long toSiteWssId;
    String toSiteLink;
    String toAddress;
    String toCity;
    String toCounty;
    String toState;
    String toHours;

    Integer driveTimeSeconds;
    Double distanceMiles;
    @Builder.Default List<Item> items = new ArrayList<>();

    /** lower numbers sort first */
    double sortScore() {
      if (distanceMiles == null) {
        return 1000.0 - items.size();
      } else {
        return distanceMiles;
      }
    }

    String getFromGoogleMapsAddress() {
      return SiteAddress.builder()
          .address(fromAddress)
          .city(fromCity)
          .state(fromState)
          .build()
          .toEncodedUrlValue();
    }

    String getGoogleMapsAddress() {
      return SiteAddress.builder()
          .address(toAddress)
          .city(toCity)
          .state(toState)
          .build()
          .toEncodedUrlValue();
    }

    String getDriveTime() {
      return driveTimeSeconds == null
          ? null
          : DurationFormatter.formatDuration(Duration.ofSeconds(driveTimeSeconds));
    }

    List<Item> getItems1() {
      List<List<Item>> splitLists =
          ListSplitter.splitItemList(
              items.stream().sorted(Comparator.comparing(i -> i.name)).toList(), 5);
      return splitLists.getFirst();
    }

    List<Item> getItems2() {
      List<List<Item>> splitLists =
          ListSplitter.splitItemList(
              items.stream().sorted(Comparator.comparing(i -> i.name)).toList(), 5);
      return splitLists.size() > 1 ? splitLists.get(1) : List.of();
    }

    void addItem(Item item) {
      this.items.add(item);
    }

    int getItemCount() {
      return items.size();
    }
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DeliveryOptionDbResult {
    long fromSiteId;
    String fromSiteName;
    long fromSiteWssId;
    String fromAddress;
    String fromCity;
    String fromCounty;
    String fromState;
    String fromHours;

    long siteId;
    String siteName;
    long toSiteWssId;
    String siteAddress;
    String city;
    String county;
    String state;
    String hours;

    String itemName;
    String urgency;
    int itemCount;
    Integer driveTimeSeconds;
    Double distanceMiles;
  }

  // TODO: improve testing
  public static List<DeliveryOption> findDeliveryOptions(
      Jdbi jdbi, Long siteWssId, String currentCounty) {
    String county =
        (currentCounty != null && currentCounty.contains(","))
            ? currentCounty.split(",")[0].trim()
            : null;
    String state =
        (currentCounty != null && currentCounty.contains(","))
            ? currentCounty.split(",")[1].trim()
            : null;

    final String whereFilter;
    final Consumer<SqlStatement<?>> bindings;

    if (siteWssId != null
        && siteWssId != 0L
        && (currentCounty == null || currentCounty.isBlank())) {
      whereFilter = "and (toSite.wss_id = :siteWssId or fromSite.wss_id = :siteWssId)\n";
      bindings = q -> q.bind("siteWssId", siteWssId);
    } else if ((siteWssId == null || siteWssId == 0L)
        && (currentCounty != null && !currentCounty.isBlank())) {
      whereFilter =
          """
          and (
              (toCounty.name = :county and toCounty.state = :state)
              or (fromCounty.name = :county and fromCounty.state = :state)
          )
        """;
      bindings =
          q ->
              q.bind("county", county) //
                  .bind("state", state);
    } else if (siteWssId != null
        && siteWssId != 0L
        && currentCounty != null
        && !currentCounty.isBlank()) {
      // both site && county selected
      whereFilter =
          """
          and (toSite.wss_id = :siteWssId or fromSite.wss_id = :siteWssId)
          and (
              (toCounty.name = :county and toCounty.state = :state)
              or (fromCounty.name = :county and fromCounty.state = :state)
          )
        """;

      bindings =
          q ->
              q.bind("siteWssId", siteWssId) //
                  .bind("county", county)
                  .bind("state", state);
    } else {
      whereFilter = "";
      bindings = _ -> {};
    }

    String query =
        String.format(
            """
         WITH needy_items AS (
            SELECT s.id as site_id, si.item_id, ist.name urgency
            FROM site_item si
            JOIN item_status ist ON si.item_status_id = ist.id
            JOIN site s on s.id = si.site_id
            WHERE ist.name IN ('Urgently Needed', 'Needed')
              and s.active = true
              and s.accepting_donations = true
        ),
        oversupply_sites AS (
            SELECT si.site_id, si.item_id
            FROM site_item si
            JOIN item_status ist ON si.item_status_id = ist.id
            JOIN site s on s.id = si.site_id
            JOIN site_type st on st.id = s.site_type_id
            WHERE
              s.active = true
              and (ist.name = 'Oversupply' or (st.name = 'Supply Hub' and ist.name in ('Available', 'Oversupply')))
        )
        SELECT
            fromSite.id fromSiteId,
            fromSite.name fromSiteName,
            fromSite.wss_id fromSiteWssId,
            fromSite.address fromAddress,
            fromSite.city fromCity,
            fromCounty.name fromCounty,
            fromCounty.state fromState,
            fromSite.hours fromHours,

            toSite.id AS siteId,
            toSite.name AS siteName,
            toSite.wss_id AS toSiteWssId,
            toSite.address AS siteAddress,
            toSite.city as city,
            toCounty.name as county,
            toCounty.state as state,
            toSite.hours as hours,

            i.name AS itemName,
            ni.urgency AS urgency,
            sdm.drive_time_seconds AS driveTimeSeconds,
            sdm.distance_miles AS distanceMiles
        FROM
            oversupply_sites fromOverSupply
        JOIN
            site fromSite ON fromOverSupply.site_id = fromSite.id
        JOIN
            county fromCounty on fromCounty.id = fromSite.county_id
        JOIN
            needy_items ni ON fromOverSupply.item_id = ni.item_id
        JOIN
            site toSite ON ni.site_id = toSite.id
        JOIN
            county toCounty ON toSite.county_id = toCounty.id
        LEFT JOIN
            site_distance_matrix sdm on
              (sdm.site1_id = ni.site_id and sdm.site2_id = fromOverSupply.site_id) or
              (sdm.site2_id = ni.site_id and sdm.site1_id = fromOverSupply.site_id)
        JOIN
            item i ON fromOverSupply.item_id = i.id
        WHERE 1 = 1
        %s
        order by lower(i.name)
        """,
            whereFilter);

    List<DeliveryOptionDbResult> dbResults =
        jdbi.withHandle(
            handle -> {
              Query qb = handle.createQuery(query);
              bindings.accept(qb);
              return qb.mapToBean(DeliveryOptionDbResult.class).list();
            });

    return aggregate(dbResults);
  }

  static List<BrowseRoutesController.Site> fetchSites(Jdbi jdbi) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
                          select wss_id, name siteName
                          from site
                          where active = true
                          order by name;
                        """)
                .mapToBean(BrowseRoutesController.Site.class)
                .list());
  }
}
