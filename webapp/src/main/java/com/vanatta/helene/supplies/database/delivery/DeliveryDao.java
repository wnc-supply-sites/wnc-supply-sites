package com.vanatta.helene.supplies.database.delivery;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class DeliveryDao {

  public static void upsert(Jdbi jdbi, DeliveryController.DeliveryUpdate deliveryUpdate) {
    String upsert =
        """
        insert into delivery(
          from_site_id, to_site_id, delivery_status, target_delivery_date,
          dispatcher_name, dispatcher_number, driver_name, driver_number,
          driver_license_plates, airtable_id, dispatcher_notes, public_url_key,
          pickup_site_name, pickup_contact_name, pickup_contact_phone,
          pickup_hours, pickup_address, pickup_city, pickup_state,
          dropoff_site_name, dropoff_contact_name, dropoff_contact_phone,
          dropoff_hours, dropoff_address, dropoff_city, dropoff_state)
        values(
          (select id from site where wss_id = :fromSiteWssId),
          (select id from site where wss_id = :toSiteWssId),
          :deliveryStatus,
          to_date(:targetDeliveryDate, 'YYYY-MM-DD'),
          :dispatcherName,
          :dispatcherNumber,
          :driverName,
          :driverNumber,
          :driverLicensePlateNumbers,
          :airtableId,
          :dispatcherNotes,
          :publicUrlKey,
          :pickupSiteName,
          :pickupContactName,
          :pickupContactPhone,
          :pickupHours,
          :pickupAddress,
          :pickupCity,
          :pickupState,
          :dropoffSiteName,
          :dropoffContactName,
          :dropoffContactPhone,
          :dropoffHours,
          :dropoffAddress,
          :dropoffCity,
          :dropoffState
        ) on conflict(airtable_id) do update set
          from_site_id = (select id from site where wss_id = :fromSiteWssId),
          to_site_id = (select id from site where wss_id = :toSiteWssId),
          delivery_status = :deliveryStatus,
          target_delivery_date = to_date(:targetDeliveryDate, 'YYYY-MM-DD'), -- 2024-12-13 SELECT TO_DATE('20170103','YYYYMMDD');
          dispatcher_name = :dispatcherName,
          dispatcher_number = :dispatcherNumber,
          driver_name = :driverName,
          driver_number = :driverNumber,
          driver_license_plates = :driverLicensePlateNumbers,
          dispatcher_notes = :dispatcherNotes,
          pickup_site_name = :pickupSiteName,
          pickup_contact_name = :pickupContactName,
          pickup_contact_phone = :pickupContactPhone,
          pickup_hours = :pickupHours,
          pickup_address = :pickupAddress,
          pickup_city = :pickupCity,
          pickup_state = :pickupState,
          dropoff_site_name = :dropoffSiteName,
          dropoff_contact_name = :dropoffContactName,
          dropoff_contact_phone = :dropoffContactPhone,
          dropoff_hours = :dropoffHours,
          dropoff_address = :dropoffAddress,
          dropoff_city = :dropoffCity,
          dropoff_state = :dropoffState
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(upsert)
                .bind(
                    "fromSiteWssId",
                    deliveryUpdate.getPickupSiteWssId().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupSiteWssId().getFirst())
                .bind(
                    "toSiteWssId",
                    deliveryUpdate.getDropOffSiteWssId().isEmpty()
                        ? null
                        : deliveryUpdate.getDropOffSiteWssId().getFirst())
                .bind("deliveryStatus", deliveryUpdate.getDeliveryStatus())
                .bind("targetDeliveryDate", deliveryUpdate.getTargetDeliveryDate())
                .bind(
                    "dispatcherName",
                    deliveryUpdate.getDispatcherName().isEmpty()
                        ? null
                        : deliveryUpdate.getDispatcherName().getFirst())
                .bind(
                    "dispatcherNumber",
                    deliveryUpdate.getDispatcherNumber().isEmpty()
                        ? null
                        : deliveryUpdate.getDispatcherNumber().getFirst())
                .bind(
                    "driverName",
                    deliveryUpdate.getDriverName().isEmpty()
                        ? null
                        : deliveryUpdate.getDriverName().getFirst())
                .bind(
                    "driverNumber",
                    deliveryUpdate.getDriverNumber().isEmpty()
                        ? null
                        : deliveryUpdate.getDriverNumber().getFirst())
                .bind(
                    "driverLicensePlateNumbers",
                    deliveryUpdate.getLicensePlateNumbers().isEmpty()
                        ? null
                        : deliveryUpdate.getLicensePlateNumbers().getFirst())
                .bind("airtableId", deliveryUpdate.getDeliveryId())
                .bind("dispatcherNotes", deliveryUpdate.getDispatcherNotes())
                .bind("publicUrlKey", deliveryUpdate.getPublicUrlKey())
                .bind(
                    "pickupSiteName",
                    deliveryUpdate.getPickupSiteName().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupSiteName().getFirst())
                .bind(
                    "pickupContactName",
                    deliveryUpdate.getPickupContactName().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupContactName().getFirst())
                .bind(
                    "pickupContactPhone",
                    deliveryUpdate.getPickupContactPhone().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupContactPhone().getFirst())
                .bind(
                    "pickupHours",
                    deliveryUpdate.getPickupHours().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupHours().getFirst())
                .bind(
                    "pickupAddress",
                    deliveryUpdate.getPickupAddress().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupAddress().getFirst())
                .bind(
                    "pickupCity",
                    deliveryUpdate.getPickupCity().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupCity().getFirst())
                .bind(
                    "pickupState",
                    deliveryUpdate.getPickupState().isEmpty()
                        ? null
                        : deliveryUpdate.getPickupState().getFirst())
                .bind(
                    "dropoffSiteName",
                    deliveryUpdate.getDropoffSiteName().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffSiteName().getFirst())
                .bind(
                    "dropoffContactName",
                    deliveryUpdate.getDropoffContactName().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffContactName().getFirst())
                .bind(
                    "dropoffContactPhone",
                    deliveryUpdate.getDropoffContactPhone().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffContactPhone().getFirst())
                .bind(
                    "dropoffHours",
                    deliveryUpdate.getDropoffHours().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffHours().getFirst())
                .bind(
                    "dropoffAddress",
                    deliveryUpdate.getDropoffAddress().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffAddress().getFirst())
                .bind(
                    "dropoffCity",
                    deliveryUpdate.getDropoffCity().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffCity().getFirst())
                .bind(
                    "dropoffState",
                    deliveryUpdate.getDropoffState().isEmpty()
                        ? null
                        : deliveryUpdate.getDropoffState().getFirst())
                .execute());

    String deletePreviousItems =
        """
      delete from delivery_item where delivery_id =
        (select id from delivery where airtable_id = :deliveryId)
    """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(deletePreviousItems)
                .bind("deliveryId", deliveryUpdate.getDeliveryId())
                .execute());

    // insert all the latest items
    String insert =
        """
    insert into delivery_item(delivery_id, item_id)
    values(
      (select id from delivery where airtable_id = :airtableId),
      (select id from item where wss_id = :itemWssId)
    )
    """;
    List<Long> itemIds = deliveryUpdate.getItemListWssIds();
    for (long itemWssId : itemIds) {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insert)
                  .bind("airtableId", deliveryUpdate.getDeliveryId())
                  .bind("itemWssId", itemWssId)
                  .execute());
    }
    // insert items that are provided by name (sometimes items won't have a WSS-ID)
    String insertByName =
        """
    insert into delivery_item(delivery_id, item_name)
    values(
      (select id from delivery where airtable_id = :airtableId),
      :itemName
    )
    """;
    List<String> itemNames = deliveryUpdate.getItemList();
    for (String itemName : itemNames) {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insertByName)
                  .bind("airtableId", deliveryUpdate.getDeliveryId())
                  .bind("itemName", itemName)
                  .execute());
    }
  }

  // get
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DeliveryData {
    long deliveryId;
    String publicUrlKey;
    String deliveryStatus;
    String dispatcherName;
    String dispatcherNumber;
    String dispatcherNotes;
    String driverName;
    String driverNumber;
    String licensePlateNumbers;
    String targetDeliveryDate;

    String fromSiteName;
    Long fromSiteId;
    private String fromAddress;
    private String fromCity;
    private String fromState;
    private String fromContactName;
    private String fromContactPhone;
    private String fromHours;

    String toSiteName;
    Long toSiteId;
    private String toAddress;
    private String toCity;
    private String toState;
    private String toContactName;
    private String toContactPhone;
    private String toHours;
  }

  public static Delivery fetchDeliveryByPublicKey(Jdbi jdbi, String publicUrlKey) {
    String whereClause = "d.public_url_key = :id";
    var results = fetchDeliveries(jdbi, whereClause, publicUrlKey);
    if (results.isEmpty()) {
      log.warn("Failed to fetch delivery by id (record not found): {}", publicUrlKey);
      throw new IllegalArgumentException("Invalid delivery ID: " + publicUrlKey);
    } else {
      return results.getFirst();
    }
  }

  public static List<Delivery> fetchDeliveriesBySiteId(Jdbi jdbi, Long siteId) {
    String whereClause =
        """
    d.from_site_id = :id
    or d.to_site_id = :id
    """;
    return fetchDeliveries(jdbi, whereClause, siteId);
  }

  private static List<Delivery> fetchDeliveries(Jdbi jdbi, String whereClause, Object idValue) {

    String select =
        String.format(
            """
    select
      d.airtable_id deliveryId,
      d.public_url_key publicUrlKey,
      d.delivery_status deliveryStatus,
      d.target_delivery_date targetDeliveryDate,
      d.dispatcher_name dispatcherName,
      d.dispatcher_number dispatcherNumber,
      d.dispatcher_notes dispatcherNotes,
      d.driver_name driverName,
      d.driver_number driverNumber,
      d.driver_license_plates licensePlateNumbers,

      coalesce(fromSite.name, d.pickup_site_name) fromSiteName,
      fromSite.id fromSiteId,
      coalesce(fromSite.address, d.pickup_address) fromAddress,
      coalesce(fromSite.city, d.pickup_city) fromCity,
      coalesce(fromCounty.state, d.pickup_state) fromState,
      coalesce(fromSite.contact_name, d.pickup_contact_name) fromContactName,
      coalesce(fromSite.contact_number, d.pickup_contact_phone) fromContactPhone,
      coalesce(fromSite.hours, d.pickup_hours) fromHours,

      coalesce(toSite.name, d.dropoff_site_name) toSiteName,
      toSite.id toSiteId,
      coalesce(toSite.address, d.dropoff_address) toAddress,
      coalesce(toSite.city, d.dropoff_city) toCity,
      coalesce(toCounty.state, d.dropoff_state) toState,
      coalesce(toSite.contact_name, d.dropoff_contact_name) toContactName,
      coalesce(toSite.contact_number, d.dropoff_contact_phone) toContactPhone,
      coalesce(toSite.hours, d.dropoff_hours) toHours
    from delivery d
    left join site fromSite on fromSite.id = d.from_site_id
    left join county fromCounty on fromCounty.id = fromSite.county_id
    left join site toSite on toSite.id = d.to_site_id
    left join county toCounty on toCounty.id = toSite.county_id
    where (%s)
    order by d.target_delivery_date desc
    """,
            whereClause);
    List<Delivery> deliveries =
        jdbi
            .withHandle(
                handle ->
                    handle
                        .createQuery(select)
                        .bind("id", idValue)
                        .mapToBean(DeliveryData.class)
                        .list())
            .stream()
            .map(Delivery::new)
            .toList();

    String selectDeliveryItems =
        """
      select distinct A.name
      from
      (
      select
        i.name
      from delivery_item di
      join item i on i.id = di.item_id
      where di.delivery_id = (select id from delivery where airtable_id = :deliveryId)
      union
      select
        di.item_name name
      from delivery_item di
      where di.delivery_id = (select id from delivery where airtable_id = :deliveryId)
      ) A
      order by A.name;
      """;

    for (Delivery delivery : deliveries) {
      List<String> items =
          jdbi.withHandle(
              handle ->
                  handle
                      .createQuery(selectDeliveryItems)
                      .bind("deliveryId", delivery.getDeliveryNumber())
                      .mapTo(String.class)
                      .list());
      delivery.getItemList().addAll(items.stream().filter(Objects::nonNull).sorted().toList());
    }

    return deliveries;
  }
}
