package com.vanatta.helene.supplies.database.volunteer;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import com.vanatta.helene.supplies.database.volunteer.VolunteerController.SiteSelect;
import com.vanatta.helene.supplies.database.volunteer.VolunteerController.Site;
import com.vanatta.helene.supplies.database.volunteer.VolunteerController.Item;
import com.vanatta.helene.supplies.database.volunteer.VolunteerController.DeliveryForm;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.util.List;

@Slf4j
public class VolunteerDao {

  static List<SiteSelect> fetchSiteSelect(Jdbi jdbi, List<String>states) {
    // todo: Write test

    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
            """
                select s.id, s.name, c.name as county, c.state
                from site s
                join county c on c.id = s.county_id
                where
                c.state in (<states>)
                and
                s.publicly_visible
                and
                s.active
                and exists (
                  select 1
                  from site_item si
                  join item_status ist on si.item_status_id = ist.id
                  Where 
                  si.site_id = s.id 
                  and 
                  ist.is_need = true)
                order by lower(s.name)
                """)
                .bindList("states", states)
                .mapToBean(SiteSelect.class)
                .list());
  }


  static Site fetchSiteItems(Jdbi jdbi, Long siteId) {
    // todo: Write test

    return  jdbi.withHandle(handle -> {
          Site site = handle.createQuery("""
                select s.id, s.name, s.address ,c.name as county, c.state
                from site s
                join county c on c.id = s.county_id
                where
                s.id = :siteId
                and 
                s.active
                and 
                s.publicly_visible
              """)
              .bind("siteId", siteId)
              .mapToBean(Site.class)
              .findOne()
              .orElse(null);

          if (site == null) {
            return null;
          };

          List<Item> items = handle.createQuery("""
              select si.id as id, i.name as name, ist.name as status
              from site_item si
              join item i on si.item_id = i.id
              join item_status ist on si.item_status_id = ist.id
              where si.site_id = :siteId
              and ist.is_need
              order by ist.sort_order, i.name
              """)
              .bind("siteId", siteId)
              .mapToBean(VolunteerController.Item.class)
              .list();
          site.setItems(items);
          return site;
    });
  }

  static Long createVolunteerDelivery(Jdbi jdbi, DeliveryForm form) {
    // todo: Write test

    // Create Delivery
    String insertDelivery = """
          INSERT INTO volunteer_delivery (
            volunteer_name,
            volunteer_phone,
            site_id,
            url_key
          ) values (
            :volunteerName,
            :volunteerPhone,
            :siteId,
            :URLKey
          )
        """;

    try {
      long deliveryId = jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insertDelivery)
                  .bind("volunteerName", form.getVolunteerName())
                  .bind("volunteerPhone", form.getVolunteerContact())
                  .bind("siteId", Integer.parseInt(form.getSite()))
                  .bind("URLKey", form.getUrlKey())
                  .executeAndReturnGeneratedKeys("id")
                  .mapTo(Long.class)
                  .one()
      );
      log.info("Creating Volunteer Delivery in DB: {}", deliveryId);

      // Create Delivery Item
      String insertItem = """
                        INSERT INTO volunteer_delivery_item (
                          site_item_id,
                          volunteer_delivery_id
                        ) VALUES (
                          :site_item_id,
                          :volunteerDeliveryId
                        )
                        """;

      for (Long itemId : form.getNeededItems()) {
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(insertItem)
                    .bind("site_item_id", itemId)
                    .bind("volunteerDeliveryId", deliveryId)
                    .execute()
        );
      }

      return deliveryId;
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw e;
    }
  }

}
