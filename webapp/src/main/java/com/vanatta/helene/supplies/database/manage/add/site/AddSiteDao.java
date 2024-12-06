package com.vanatta.helene.supplies.database.manage.add.site;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class AddSiteDao {

  public static class DuplicateSiteException extends RuntimeException {
    DuplicateSiteException(String message) {
      super(message);
    }
  }

  /**
   * Adds a new site and returns the ID of that site.
   *
   * @throws DuplicateSiteException Thrown if site name already exists
   * @throws IllegalArgumentException If an invalid county is specified
   * @throws UnableToExecuteStatementException if required fields are missing
   */
  public static long addSite(Jdbi jdbi, AddSiteData siteData) {
    String insert =
        """
        insert into site(
          name,
          address,
          city,
          county_id,
          website,
          facebook,
          site_type_id,
          hours,
          contact_name,
          contact_number,
          contact_email,
          additional_contacts
        ) values(
          :siteName,
          :address,
          :city,
          (select id from county where name = :countyName and state = :state),
          :website,
          :facebook,
          (select id from site_type where name = :siteType),
          :hours,
          :contactName,
          :contactNumber,
          :contactEmail,
          :additionalContacts
         )
        """;

    try {
      return jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insert)
                  .bind("siteName", siteData.getSiteName())
                  .bind("address", siteData.getStreetAddress())
                  .bind("city", siteData.getCity())
                  .bind("countyName", siteData.getCounty())
                  .bind("state", siteData.getState())
                  .bind("website", siteData.getWebsite())
                  .bind("facebook", siteData.getFacebook())
                  .bind("siteType", siteData.getSiteType().getText())
                  .bind("hours", siteData.getSiteHours())
                  .bind("contactName", siteData.getContactName())
                  .bind("contactNumber", siteData.getContactNumber())
                  .bind("contactEmail", siteData.getContactEmail())
                  .bind("additionalContacts", siteData.getAdditionalContacts())
                  .executeAndReturnGeneratedKeys("id")
                  .mapTo(Long.class)
                  .one());
    } catch (UnableToExecuteStatementException e) {
      if (e.getMessage()
          .contains("duplicate key value violates unique constraint \"site_name_key\"")) {
        throw new DuplicateSiteException(
            "Duplicate, site name already exists: " + siteData.getSiteName());
      } else if (e.getMessage().contains("null value in column \"county_id\"")) {
        throw new IllegalArgumentException("Invalid county specified: " + siteData.getCounty());
      } else {
        throw e;
      }
    }
  }
}
