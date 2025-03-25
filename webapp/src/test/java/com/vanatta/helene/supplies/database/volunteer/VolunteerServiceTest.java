package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.util.URLKeyGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.List;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

public class VolunteerServiceTest {

  private VolunteerService volunteerService;

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void errorIfItemIdDoesNotExist() {
    long siteId = TestConfiguration.getSiteId("site1");

    VolunteerService.Site site = VolunteerDao.fetchSiteItems(jdbiTest, siteId);

    List<Long> itemIds = new java.util.ArrayList<>(site.getItems().stream().map(VolunteerService.Item::getId).toList());

    // Adding fake item id to itemIds
    itemIds.add(-66L);

    String urlKey = URLKeyGenerator.generateUrlKey();

    VolunteerService.DeliveryForm form = VolunteerService.DeliveryForm.builder()
        .site(String.valueOf(siteId))
        .neededItems(itemIds)
        .volunteerContact("1231231234")
        .volunteerName("John Test")
        .urlKey(urlKey)
        .build();

    Exception exception = Assertions.assertThrows(
        RuntimeException.class,
        () -> volunteerService.createVolunteerDelivery(jdbiTest, form),
        "Expected an exception due to Invalid item id URL Key."
    );

    // Check that the created delivery was removed after error
    List<VolunteerService.VolunteerDeliveryItem> deliveryItems = jdbiTest.withHandle(handle ->
        handle.createQuery("""
            SELECT *
            FROM volunteer_delivery
            WHERE volunteer_delivery.site_id = :siteId
            """)
            .bind("siteId", siteId)
            .mapToBean(VolunteerService.VolunteerDeliveryItem.class)
            .list());
    assertThat(deliveryItems.isEmpty()).isTrue();

  }
}
