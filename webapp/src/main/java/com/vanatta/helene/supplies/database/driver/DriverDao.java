package com.vanatta.helene.supplies.database.driver;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.util.TruncateString;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class DriverDao {

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Driver {
    Long airtableId;
    private String fullName;
    private String phone;
    private boolean active;
    private boolean blacklisted;
    private String location;
    private String availability;
    private String comments;
    private String licensePlates;

    public String getComments() {
      return TruncateString.truncate(comments, 1000);
    }

    public String getAvailability() {
      return TruncateString.truncate(availability, 1000);
    }

    static Driver parseJson(String json) {
      return new Gson().fromJson(json, Driver.class);
    }
  }

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
                      comments
                    from driver where phone = :phone
                    """)
                .bind("phone", phoneNumber)
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
                  comments, availability)
            values(
               :airtableId,
               :name,
               :phone,
               :location,
               :active,
               :blacklisted,
               :licensePlates,
               :comments,
               :availability
            ) on conflict(airtable_id) do update set
               name = :name,
               phone = :phone,
               location = :location,
               active = :active,
               black_listed = :blacklisted,
               license_plates = :licensePlates,
               comments = :comments,
               availability = :availability
            """)
                .bind("airtableId", driver.getAirtableId())
                .bind("name", driver.getFullName())
                .bind("phone", driver.getPhone())
                .bind("location", driver.getLocation())
                .bind("active", driver.isActive())
                .bind("blacklisted", driver.isBlacklisted())
                .bind("licensePlates", driver.getLicensePlates())
                .bind("comments", driver.getComments())
                .bind("availability", driver.getAvailability())
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
                      update driver set %s = :newValue where airtable_id = :airtableId
                      """,
                        driverUpdate.columnToUpdate()))
                .bind("newValue", driverUpdate.getNewValue())
                .bind("airtableId", driverUpdate.getAirtableId())
                .execute());
  }
}
