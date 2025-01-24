package com.vanatta.helene.supplies.database.driver;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class DriverDao {

  public static Optional<Driver> lookupByPhone(Jdbi jdbi, String phoneNumber) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
                    select
                      airtable_id,
                      name fullName,
                      phone,
                      active,
                      black_listed,
                      location,
                      license_plates,
                      availability,
                      comments,
                      can_lift_50lbs
                    from driver where regexp_replace(phone, '[^0-9]+', '', 'g') = :phone
                    """)
                .bind("phone", PhoneNumberUtil.removeNonNumeric(phoneNumber))
                .mapToBean(Driver.class)
                .findOne());
  }

  public static void upsert(Jdbi jdbi, Driver driver) {
    jdbi.withHandle(
        h ->
            h.createUpdate(
                    """
            insert into driver(
                  airtable_id, name, phone, location,
                  active, black_listed, license_plates,
                  comments, availability, can_lift_50lbs)
            values(
               :airtableId,
               :name,
               :phone,
               :location,
               :active,
               :blacklisted,
               :licensePlates,
               :comments,
               :availability,
               :can_lift_50lbs
            ) on conflict(airtable_id) do update set
               name = :name,
               phone = :phone,
               location = :location,
               active = :active,
               black_listed = :blacklisted,
               license_plates = :licensePlates,
               comments = :comments,
               availability = :availability,
               can_lift_50lbs = :can_lift_50lbs
            """)
                .bind("airtableId", driver.getAirtableId())
                .bind("name", driver.getFullName())
                .bind("phone", PhoneNumberUtil.removeNonNumeric(driver.getPhone()))
                .bind("location", driver.getLocation())
                .bind("active", driver.isActive())
                .bind("blacklisted", driver.isBlacklisted())
                .bind("licensePlates", driver.getLicensePlates())
                .bind("comments", driver.getComments())
                .bind("availability", driver.getAvailability())
                .bind("can_lift_50lbs", driver.isCan_lift_50lbs())
                .execute());
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class DriverUpdate {
    long airtableId;
    String fieldName;
    String newValue;

    String columnToUpdate() {
      return switch (fieldName) {
        case "active" -> "active";
        case "licensePlates" -> "license_plates";
        case "blacklisted" -> "black_listed";
        default -> throw new IllegalStateException("Unexpected value: " + fieldName);
      };
    }

    Object getNewValue() {
      return switch (fieldName) {
        case "licensePlates" -> newValue;
        case "active", "blacklisted" -> newValue != null;
        default -> throw new IllegalStateException("Unexpected value: " + fieldName);
      };
    }

    static DriverUpdate parseJson(String json) {
      return new Gson().fromJson(json, DriverUpdate.class);
    }
  }

  @SuppressWarnings("SqlSourceToSinkFlow")
  static void update(Jdbi jdbi, DriverUpdate driverUpdate) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    String.format(
                        """
                      update driver set %s = :newValue, last_updated = now() where airtable_id = :airtableId
                      """,
                        driverUpdate.columnToUpdate()))
                .bind("newValue", driverUpdate.getNewValue())
                .bind("airtableId", driverUpdate.getAirtableId())
                .execute());
  }

  static void toggleActiveStatus(Jdbi jdbi, String phone) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                        update driver set
                          active = (
                            select not active
                            from driver
                            where regexp_replace(phone, '[^0-9]+', '', 'g') = :phone
                          ),
                          last_updated = now()
                        where phone = :phone
                        """)
                .bind("phone", PhoneNumberUtil.removeNonNumeric(phone))
                .execute());
  }
}
