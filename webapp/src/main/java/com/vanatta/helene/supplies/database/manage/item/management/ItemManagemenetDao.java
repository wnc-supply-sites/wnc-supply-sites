package com.vanatta.helene.supplies.database.manage.item.management;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class ItemManagemenetDao {

  // TODO: handle null case (can be possible if a person is using multiple browser windows
  static ItemStatus fetchItemStatus(Jdbi jdbi, long siteId, String itemName) {
    String query = """
        select ist.name
        from item_status ist
        join site_item si on si.item_status_id = ist.id
        join item i on i.id = si.item_id
        where i.name = :itemName and si.site_id = :siteId
        """;
    String status = jdbi.withHandle(handle -> handle.createQuery(query)
        .bind("siteId", siteId)
        .bind("itemName", itemName)
        .mapTo(String.class)
        .one());
    return ItemStatus.fromTextValue(status);
  }

  public static void updateSiteItemActive(Jdbi jdbi, long siteId, String itemName, String itemStatus) {
    String insert =
        """
          insert into site_item(site_id, item_id, item_status_id) values
             (
                :siteId,
                (select id from item where name = :itemName),
                (select id from item_status where name = :itemStatus)
             )
          """;
    try {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insert)
                  .bind("siteId", siteId)
                  .bind("itemName", itemName)
                  .bind("itemStatus", itemStatus)
                  .execute());
    } catch (Exception e) {
      if (e.getMessage().contains("already exists.")
          || (e.getCause() != null && e.getCause().getMessage().contains("duplicate key value"))) {
        log.warn(
            "Duplicate key insert attempted, siteId: {}, itemName: {}, itemStatus: {}",
            siteId,
            itemName,
            itemStatus);
      } else {
        throw e;
      }
    }
    ManageSiteDao.updateSiteLastUpdatedToNow(jdbi, siteId);
  }

  static void updateSiteItemInactive(Jdbi jdbi, long siteId, String itemName) {
    String delete =
        """
            delete from site_item
            where site_id = :siteId
              and item_id = (select id from item where name = :itemName)
            """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(delete)
                .bind("siteId", siteId)
                .bind("itemName", itemName)
                .execute());
    ManageSiteDao.updateSiteLastUpdatedToNow(jdbi, siteId);
  }

  public static void updateItemStatus(Jdbi jdbi, long siteId, String itemName, String itemStatus) {
    if (!ItemStatus.allItemStatus().contains(itemStatus)) {
      throw new IllegalArgumentException("Invalid item status: " + itemStatus);
    }

    String update =
        """
      update site_item
      set item_status_id = (select id from item_status where name = :itemStatus),
         last_updated = now()
      where site_id = :siteId
         and item_id = (select id from item where name = :itemName)
      """;
    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(update)
                    .bind("siteId", siteId)
                    .bind("itemName", itemName)
                    .bind("itemStatus", itemStatus)
                    .execute());

    if (updateCount != 1) {
      throw new IllegalArgumentException(String.format("Invalid item name: %s", itemName));
    }
    ManageSiteDao.updateSiteLastUpdatedToNow(jdbi, siteId);
  }


  /** Adds a brand new item to database, inserts into item table. */
  public static boolean addNewItem(Jdbi jdbi, String itemName) {
    String insert = "insert into item(name) values(:itemName)";

    try {
      jdbi.withHandle(handle -> handle.createUpdate(insert).bind("itemName", itemName).execute());
    } catch (Exception e) {
      if (e.getMessage().contains("duplicate key")) {
        return false;
      } else {
        throw e;
      }
    }
    return true;
  }


}
