package com.vanatta.helene.supplies.database.volunteer;

import static com.vanatta.helene.supplies.database.TestConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.util.URLKeyGenerator;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class VolunteerDaoTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  /** fetchSiteSelect */

  // todo: fetchSiteSelect -> Does not contain inactive sites
  @Test
  void retrievesSiteSelect() {
    List<VolunteerController.SiteSelect> results = VolunteerDao.fetchSiteSelect(jdbiTest, List.of("NC","KY", "VA", "TN"));

    // Retrieves sites that are active and accepting donations
    assertThat(results.size() == 1).isTrue();

    // Add a site without items
    String inactiveSiteName = addSite("inactiveSite");
    Long inactiveSiteId = TestConfiguration.getSiteId(inactiveSiteName);

    List<VolunteerController.SiteSelect> updatedResults = VolunteerDao.fetchSiteSelect(jdbiTest, List.of("NC","KY", "VA", "TN"));

    // Validate that updated result and results are the same size
    assertThat(results.size() == updatedResults.size()).isTrue();
  }

  /** fetchSiteItems */

  @Test
  void correctlyRetrievesNeededItems() {
    long siteId = TestConfiguration.getSiteId("site1");

    VolunteerController.Site result = VolunteerDao.fetchSiteItems(jdbiTest, siteId);

    // Retrieved site name is correct
    assertThat(result.getName()).isEqualTo("site1");

    // Retrieved item count is one
    assertThat(result.getItems().size() == 1).isTrue();

    addItemToSite(siteId, ItemStatus.NEEDED, "heater", -80);

    // Update site item
    VolunteerController.Site resultAfterUpdate = VolunteerDao.fetchSiteItems(jdbiTest, siteId);

    // Check site item count was increased
    assertThat(resultAfterUpdate.getItems().size() == 2).isTrue();
  }

  @Test
  void neededItemsRequestReturnsNull() {
    // Returns null if that item id does not exist
    VolunteerController.Site result = VolunteerDao.fetchSiteItems(jdbiTest, 567L);
    assertThat(result).isNull();
  }

  /** createVolunteerDelivery */
  @Test
  void createsVolunteerDelivery(){
    long siteId = TestConfiguration.getSiteId("site1");

    VolunteerController.Site site = VolunteerDao.fetchSiteItems(jdbiTest, siteId);

    List<Long> itemIds = site.getItems().stream().map(VolunteerController.Item::getId).toList();

    String urlKey = URLKeyGenerator.generateUrlKey();

    VolunteerController.DeliveryForm form = VolunteerController.DeliveryForm.builder()
        .site(String.valueOf(siteId))
        .neededItems(itemIds)
        .volunteerContact("1231231234")
        .volunteerName("John Test")
        .urlKey(urlKey)
        .build();

    Long volunteerDeliveryId = VolunteerDao.createVolunteerDelivery(jdbiTest, form);

    assertThat(volunteerDeliveryId).isNotNull();

     VolunteerDao.VolunteerDelivery volunteerDelivery = jdbiTest.withHandle(handle ->
         handle.createQuery("""
        SELECT id, volunteer_name, volunteer_phone, site_id, url_key 
        FROM volunteer_delivery 
        WHERE id = :id 
        LIMIT 1
    """)
             .bind("id", volunteerDeliveryId)
             .mapToBean(VolunteerDao.VolunteerDelivery.class)
             .one()
     );

     // Check that the created volunteer delivery row has the correct information
     assertThat(
         Objects.equals(
             volunteerDelivery,
             VolunteerDao.VolunteerDelivery
                 .builder()
                 .id(volunteerDeliveryId)
                 .volunteerPhone("1231231234")
                 .volunteerName("John Test")
                 .siteId(siteId)
                 .urlKey(urlKey)
                 .build())).isTrue();
  }

  @Test
  void errorIfURLKeyIsNotUnique() {
    long siteId = TestConfiguration.getSiteId("site1");

    VolunteerController.Site site = VolunteerDao.fetchSiteItems(jdbiTest, siteId);

    List<Long> itemIds = site.getItems().stream().map(VolunteerController.Item::getId).toList();

    String urlKey = URLKeyGenerator.generateUrlKey();

    VolunteerController.DeliveryForm form1 = VolunteerController.DeliveryForm.builder()
        .site(String.valueOf(siteId))
        .neededItems(itemIds)
        .volunteerContact("1231231234")
        .volunteerName("John Test")
        .urlKey(urlKey)
        .build();

    VolunteerController.DeliveryForm form2 = VolunteerController.DeliveryForm.builder()
        .site(String.valueOf(siteId))
        .neededItems(itemIds)
        .volunteerContact("2223334444")
        .volunteerName("Sally Test")
        .urlKey(urlKey)
        .build();

    Long volunteerDeliveryId = VolunteerDao.createVolunteerDelivery(jdbiTest, form1);

    Exception exception = Assertions.assertThrows(
        UnableToExecuteStatementException.class,
        () -> VolunteerDao.createVolunteerDelivery(jdbiTest, form2),
        "Expected an exception due to duplicate URL Key."
    );

    assertThat(exception.getMessage().contains("Error: Duplicate URL key")).isTrue();
  }

  // todo: createVolunteerDelivery -> Returns an error if itemId does not exist

  // todo: createVolunteerDelivery -> Creates a new entry in volunteer_delivery

  // todo: createVolunteerDelivery -> Creates new volunteer_delivery_items

  // todo: createVolunteerDelivery -> If an error occurs when creating items, delete the items

  /** getVolunteerDelivery */
}
