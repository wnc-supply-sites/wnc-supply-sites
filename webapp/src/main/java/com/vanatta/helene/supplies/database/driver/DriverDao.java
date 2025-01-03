package com.vanatta.helene.supplies.database.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

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
    private boolean optedOut;
    private String location;
    private String licensePlates;
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
                      opted_out,
                      location,
                      license_plates
                    from driver where phone = :phone
                    """)
                .bind("phone", phoneNumber)
                .mapToBean(Driver.class)
                .findOne());
  }

  public static void upsert(Jdbi jdbi, Driver driver) {
    jdbi.withHandle(h -> h.createUpdate("""
            insert into driver(airtable_id, name, phone, location, active, opted_out, license_plates)
            values(
               :airtableId,
               :name,
               :phone,
               :location,
               true,
               false,
               :licensePlates
            ) on conflict(airtable_id) do update set
               name = :name,
               phone = :phone,
               location = :location,
               active = :active,
               opted_out = :optedOut,
               license_plates = :licensePlates
            """)
        .bind("airtableId", driver.getAirtableId())
        .bind("name", driver.getFullName())
        .bind("phone", driver.getPhone())
        .bind("location", driver.getLocation())
        .bind("active", driver.isActive())
        .bind("optedOut", driver.isOptedOut())
        .bind("licensePlates", driver.getLicensePlates())
        .execute()
    );
  }
}
