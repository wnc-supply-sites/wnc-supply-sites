package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.util.SecretCodeGenerator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class DeliveryDao {

  public static void upsert(Jdbi jdbi, DeliveryUpdate deliveryUpdate) {
    String upsert =
        """
        insert into delivery(
          from_site_id, to_site_id, delivery_status, target_delivery_date,
          dispatcher_name, dispatcher_number, driver_name, driver_number,
          driver_license_plates, airtable_id, dispatcher_notes, public_url_key,
          dispatch_code, driver_code,
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
          :dispatchCode,
          :driverCode,
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
          dispatch_code = :dispatchCode,
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
                .bind("dispatcherName", firstValue(deliveryUpdate.getDispatcherName()))
                .bind("dispatcherNumber", firstValue(deliveryUpdate.getDispatcherNumber()))
                .bind("driverName", firstValue(deliveryUpdate.getDriverName()))
                .bind("driverNumber", firstValue(deliveryUpdate.getDriverNumber()))
                .bind(
                    "driverLicensePlateNumbers",
                    firstValue(deliveryUpdate.getLicensePlateNumbers()))
                .bind("airtableId", deliveryUpdate.getDeliveryId())
                .bind("dispatcherNotes", deliveryUpdate.getDispatcherNotes())
                .bind("dispatchCode", deliveryUpdate.getDispatchCode())
                .bind("driverCode", SecretCodeGenerator.generateCode())
                .bind("publicUrlKey", deliveryUpdate.getPublicUrlKey())
                .bind("pickupSiteName", firstValue(deliveryUpdate.getPickupSiteName()))
                .bind("pickupContactName", firstValue(deliveryUpdate.getPickupContactName()))
                .bind("pickupContactPhone", firstValue(deliveryUpdate.getPickupContactPhone()))
                .bind("pickupHours", firstValue(deliveryUpdate.getPickupHours()))
                .bind("pickupAddress", firstValue(deliveryUpdate.getPickupAddress()))
                .bind("pickupCity", firstValue(deliveryUpdate.getPickupCity()))
                .bind("pickupState", firstValue(deliveryUpdate.getPickupState()))
                .bind("dropoffSiteName", firstValue(deliveryUpdate.getDropoffSiteName()))
                .bind("dropoffContactName", firstValue(deliveryUpdate.getDropoffContactName()))
                .bind("dropoffContactPhone", firstValue(deliveryUpdate.getDropoffContactPhone()))
                .bind("dropoffHours", firstValue(deliveryUpdate.getDropoffHours()))
                .bind("dropoffAddress", firstValue(deliveryUpdate.getDropoffAddress()))
                .bind("dropoffCity", firstValue(deliveryUpdate.getDropoffCity()))
                .bind("dropoffState", firstValue(deliveryUpdate.getDropoffState()))
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
    if (itemNames != null) {
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
  }

  private static String firstValue(List<String> input) {
    return input == null || input.isEmpty() ? null : input.getFirst();
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

    private String dispatchCode;
    private String driverStatus;

    /**
     * Driver code is used to update driverStatus. It is not used to do confirmations. The driver
     * confirm code is used for confirmations.
     */
    private String driverCode;
  }

  public static Optional<Delivery> fetchDeliveryByPublicKey(Jdbi jdbi, String publicUrlKey) {
    String whereClause = "d.public_url_key = :id";
    var results = fetchDeliveries(jdbi, whereClause, publicUrlKey);
    if (results.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(results.getFirst());
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
      coalesce(toSite.hours, d.dropoff_hours) toHours,

      d.dispatch_code,
      d.driver_status,
      d.driver_code driverCode
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

    String selectConfirmations =
        """
      select
         dc.confirm_type confirmRole,
         dc.delivery_accepted confirmed,
         dc.secret_code code
      from delivery_confirmation dc
      join delivery d on d.id = dc.delivery_id
      where d.public_url_key = :publicUrlKey
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
      delivery.addItems(items.stream().filter(Objects::nonNull).sorted().toList());

      List<DeliveryConfirmation> confirmations =
          jdbi.withHandle(
              handle ->
                  handle
                      .createQuery(selectConfirmations)
                      .bind("publicUrlKey", delivery.getPublicKey())
                      .mapToBean(DeliveryConfirmation.class)
                      .list());
      delivery.addConfirmations(confirmations);
    }

    return deliveries;
  }
}
