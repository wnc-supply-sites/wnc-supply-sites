package com.vanatta.helene.supplies.database.manage.add.site;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class AddSiteDao {

  static class DuplicateSiteException extends RuntimeException {
    DuplicateSiteException(String message) {
      super(message);
    }
  }

  public static void addSite(Jdbi jdbi, AddSiteData siteData) {
    String insert =
        """
        insert into site(
            name, address, city,
            county_id, state, contact_number,
            website, site_type_id
        ) values(
          :siteName,
          :address,
          :city,
          (select id from county where name = :countyName),
          :state,
          :contactNumber,
          :website,
          (select id from site_type where name = :siteType)
         )
        """;

    try {
      int insertCount =
          jdbi.withHandle(
              handle ->
                  handle
                      .createUpdate(insert)
                      .bind("siteName", siteData.getSiteName())
                      .bind("address", siteData.getStreetAddress())
                      .bind("city", siteData.getCity())
                      .bind("countyName", siteData.getCounty())
                      .bind("state", siteData.getState())
                      .bind("contactNumber", siteData.getContactNumber())
                      .bind("website", siteData.getWebsite())
                      .bind("siteType", siteData.getSiteType().getText())
                      .execute());

      if (insertCount != 1) {
        log.warn("Failed to insert new site: {}", siteData);
        throw new RuntimeException("Failed to insert new site");
      }
    } catch (UnableToExecuteStatementException e) {
      if(e.getMessage().contains("duplicate key value violates unique constraint \"site_name_key\"")) {
        throw new DuplicateSiteException("Duplicate, site name already exists: " + siteData.getSiteName());
      } else
      if (e.getMessage().contains("null value in column \"county_id\"")) {
        throw new IllegalArgumentException("Invalid county specified: " + siteData.getCounty());
      } else {
        throw e;
      }
    }
  }
}
