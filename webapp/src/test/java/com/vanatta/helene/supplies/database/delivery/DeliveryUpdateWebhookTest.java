package com.vanatta.helene.supplies.database.delivery;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test focuses on the delivery update webhook. That we can receive delivery updates and then store
 * that data. We will exercise methods to parse the delivery data, store and then retrieve that
 * data.
 */
class DeliveryUpdateWebhookTest {
  DeliveryUpdateWebhook deliveryUpdateWebhook = new DeliveryUpdateWebhook(jdbiTest);

  @BeforeEach
  void setupDatabase() {
    TestConfiguration.setupDatabase();
  }

  private static final String deliveryUpdateInput =
      """
     {
        "deliveryId" : 68,
        "deliveryStatus" : "Creating Dispatch",
        "dispatcherName" : [
           "Joe Doe"
        ],
        "dispatcherNumber" : [
           "919.111.1111"
        ],
        "driverName" : ["Jane Doe"],
        "driverNumber" : ["919.222.2222"],
        "dropOffSiteWssId" : [
           337
        ],
        "itemListWssIds" : [
           161,
           191,
           192,
           152
        ],
        "licensePlateNumbers" : ["XYZ-123,ABC-333"],
        "pickupSiteWssId" : [
           3088
        ],
        "targetDeliveryDate" : "2024-12-13",
        "dispatcherNotes": "notes from dispatcher",
        "publicUrlKey": "QWER",
        "dispatchCode": "DDDD"
     }
     """;

  String deliveryInput2 =
      """
  {"deliveryId":91,"itemListWssIds":[296],"driverNumber":[],"driverName":[],"dispatcherNumber":["828.279.2054"],"dispatcherName":["John"],"deliveryStatus":"Creating Dispatch","dropOffSiteWssId":[107],"pickupSiteWssId":[101],"targetDeliveryDate":null,"licensePlateNumbers":[],"publicUrlKey": "ASDF"}
  """;

  static final String deliveryInput3 =
      """
          {"updateTimestampMs":"2024-12-30T03:30:07.471Z","deliveryId":130,"itemListWssIds":[],
          "itemList":["AA Batteries"],"driverNumber":["(919) 000-0000"],"driverName":["Test"],
          "dispatcherNumber":["919.000.0000"],"dispatcherName":["Test"],"deliveryStatus":"Creating Dispatch",
          "targetDeliveryDate":"2024-12-30","licensePlateNumbers":["test-xyz,WXX-123"],"publicUrlKey":"AAAA",
          "dispatcherCode":"VAAA","dropOffSiteWssId":[337],"pickupSiteWssId":[337],
          "pickupSiteName":["zTest"],"pickupContactName":["zTest1"],"pickupContactPhone":["(919) 000-0000"],
          "pickupHours":["zTest1"],"pickupAddress":["zTest1"],"pickupCity":["zTest12"],
          "pickupState":["TN"],"dropoffSiteName":["zTest"],"dropoffContactName":["zTest1"],
          "dropoffContactPhone":["(919) 360-0528"],"dropoffHours":["zTest1"],
          "dropoffAddress":["zTest1"],"dropoffCity":["zTest12"],"dropoffState":["TN"]}
    """;

  @Test
  void canParseInput() {
    DeliveryUpdate update = DeliveryUpdate.parseJson(deliveryUpdateInput);
    assertThat(update.getDeliveryId()).isEqualTo(68L);
    assertThat(update.getDeliveryStatus()).isEqualTo("Creating Dispatch");
    assertThat(update.getDispatcherName()).containsExactly("Joe Doe");
    assertThat(update.getDispatcherNumber()).containsExactly("919.111.1111");
    assertThat(update.getDriverName()).containsExactly("Jane Doe");
    assertThat(update.getDriverNumber()).containsExactly("919.222.2222");
    assertThat(update.getDropOffSiteWssId()).containsExactly(337L);
    assertThat(update.getPickupSiteWssId()).containsExactly(3088L);
    assertThat(update.getItemListWssIds()).contains(161L, 191L, 192L, 152L);
    assertThat(update.getLicensePlateNumbers()).containsExactly("XYZ-123,ABC-333");
    assertThat(update.getTargetDeliveryDate()).isEqualTo("2024-12-13");
    assertThat(update.getDispatcherNotes()).isEqualTo("notes from dispatcher");
    assertThat(update.getDispatcherCode()).isEqualTo("DDDD");
  }

  @Test
  void parseDeliveryData() {
    var input = TestDataFile.DELIVERY_DATA_JSON.readData();
    DeliveryUpdate update = DeliveryUpdate.parseJson(input);

    assertThat(update.getDeliveryId()).isEqualTo(5);
    assertThat(update.getItemList())
        .contains(
            "Buddy heater adapter hose",
            "Toilet Paper",
            "Propane (20lb)",
            "Propane (1lb)",
            "Paper Towels",
            "Kid friendly snacks",
            "Gas Cans",
            "Flashlights",
            "Dog Food",
            "Dish Soap",
            "Cookware",
            "Bedding",
            "Baby Items");

    assertThat(update.getDriverNumber()).containsExactly("(444) 333-7022");
    assertThat(update.getDriverName()).containsExactly("Jason");
    assertThat(update.getDispatcherNumber()).containsExactly("919.000.3344");
    assertThat(update.getDispatcherName()).containsExactly("Dan");
    assertThat(update.getDeliveryStatus()).isEqualTo("Delivery Completed");
    assertThat(update.getTargetDeliveryDate()).isEqualTo("2024-12-13");
    assertThat(update.getLicensePlateNumbers()).isEmpty();
    assertThat(update.getDropOffSiteWssId()).isEmpty();
    assertThat(update.getPickupSiteWssId()).isEmpty();

    assertThat(update.getPickupSiteName()).containsExactly("Valley Hope Foundation");
    List<String> nullContainer = new ArrayList<>();
    nullContainer.add(null);
    assertThat(update.getPickupContactName()).isEqualTo(nullContainer);
    assertThat(update.getPickupContactPhone()).containsExactly("(888) 333-0000");
    assertThat(update.getPickupHours()).containsExactly("Monday - Friday \n10am - 3pm");
    assertThat(update.getPickupAddress()).containsExactly("1035 I40");
    assertThat(update.getPickupCity()).containsExactly("Black Mountain");
    assertThat(update.getPickupState()).containsExactly("NC");

    assertThat(update.getDropoffSiteName()).containsExactly("Hope");
    assertThat(update.getDropoffContactName()).containsExactly("dropoff contact");
    assertThat(update.getDropoffContactPhone()).containsExactly("(888) 222-4444");
    assertThat(update.getDropoffHours()).containsExactly("Monday - Friday \n10am - 5pm");
    assertThat(update.getDropoffAddress()).containsExactly("60 Flat");
    assertThat(update.getDropoffCity()).containsExactly("Elk Park");
    assertThat(update.getDropoffState()).containsExactly("NC");
  }

  @Test
  void canParse2() {
    DeliveryUpdate.parseJson(deliveryInput2);
  }

  @Test
  void canUpserSample3() {
    deliveryUpdateWebhook.upsertDelivery(deliveryInput3);
  }

  @Test
  void deliveryUpdateComplete() {
    var input = DeliveryUpdate.builder().deliveryStatus("Delivery Completed").build();
    assertThat(input.isComplete()).isTrue();

    input = DeliveryUpdate.builder().deliveryStatus("complete").build();
    assertThat(input.isComplete()).isTrue();

    input = DeliveryUpdate.builder().deliveryStatus(null).build();
    assertThat(input.isComplete()).isFalse();

    input = DeliveryUpdate.builder().deliveryStatus("pending").build();
    assertThat(input.isComplete()).isFalse();
  }

  @Test
  void deliveriesStored() {
    DeliveryUpdate update = DeliveryUpdate.parseJson(deliveryUpdateInput);

    var inputData =
        update.toBuilder()
            .pickupSiteWssId(List.of(TestConfiguration.SITE1_WSS_ID))
            .dropOffSiteWssId(List.of(TestConfiguration.SITE2_WSS_ID))
            .itemListWssIds(
                List.of(TestConfiguration.WATER_WSS_ID, TestConfiguration.GLOVES_WSS_ID))
            .build();

    // before we store any data, assert that there is none
    assertThat(DeliveryDao.fetchDeliveriesBySiteId(jdbiTest, TestConfiguration.getSiteId("site1")))
        .isEmpty();

    // now store the delivery
    var response = deliveryUpdateWebhook.upsertDelivery(new Gson().toJson(inputData));
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    // fetch the deliveyr
    var deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(deliveries).hasSize(1);
    var delivery = deliveries.getFirst();
    assertThat(delivery.getDeliveryNumber()).isEqualTo(68L);
    assertThat(delivery.getDeliveryStatus()).isEqualTo("Creating Dispatch");
    assertThat(delivery.getDispatcherName()).isEqualTo("Joe Doe");
    assertThat(delivery.getDispatcherPhoneNumber()).isEqualTo("919.111.1111");
    assertThat(delivery.getDriverName()).isEqualTo("Jane Doe");
    assertThat(delivery.getDriverPhoneNumber()).isEqualTo("919.222.2222");
    assertThat(delivery.getFromSite()).isEqualTo("site1");
    assertThat(delivery.getToSite()).isEqualTo("site2");
    assertThat(delivery.getItemList()).contains("gloves", "water");
    assertThat(delivery.getDriverLicensePlate()).isEqualTo("XYZ-123,ABC-333");
    assertThat(delivery.getDeliveryDate()).isEqualTo("2024-12-13");

    // validate site lookup details
    var details = SiteDetailDao.lookupSiteById(jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(delivery.getFromSite()).isEqualTo(details.getSiteName());
    assertThat(delivery.getFromAddress()).isEqualTo(details.getAddress());
    assertThat(delivery.getFromCity()).isEqualTo(details.getCity());
    assertThat(delivery.getFromState()).isEqualTo(details.getState());
    assertThat(delivery.getFromContactName()).isEqualTo(details.getContactName());
    assertThat(delivery.getFromContactPhoneNumber()).isEqualTo(details.getContactNumber());

    var toDetails = SiteDetailDao.lookupSiteById(jdbiTest, TestConfiguration.getSiteId("site2"));
    assertThat(delivery.getToSite()).isEqualTo(toDetails.getSiteName());
    assertThat(delivery.getToAddress()).isEqualTo(toDetails.getAddress());
    assertThat(delivery.getToCity()).isEqualTo(toDetails.getCity());
    assertThat(delivery.getToState()).isEqualTo(toDetails.getState());
    assertThat(delivery.getToContactName()).isEqualTo(toDetails.getContactName());
    assertThat(delivery.getToContactPhoneNumber()).isEqualTo(toDetails.getContactNumber());

    // now update the delivery that we just inserted
    var updatedInput =
        inputData.toBuilder()
            .deliveryStatus("In Progress")
            .dispatcherName(List.of("Jane Doe"))
            .dispatcherNumber(List.of("555.555.5555"))
            .driverName(List.of("driver"))
            .driverNumber(List.of("333.333.3333"))
            // we are flipping the to & from site IDs
            .pickupSiteWssId(List.of(TestConfiguration.SITE2_WSS_ID))
            .dropOffSiteWssId(List.of(TestConfiguration.SITE1_WSS_ID))
            .licensePlateNumbers(List.of("Traveler"))
            .targetDeliveryDate("2024-12-15")
            // we are reducing the number of items from 2 to 1
            .itemListWssIds(List.of(TestConfiguration.WATER_WSS_ID))
            .build();

    // now update the delivery that we just inserted
    response = deliveryUpdateWebhook.upsertDelivery(new Gson().toJson(updatedInput));
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(deliveries).hasSize(1);
    delivery = deliveries.getFirst();
    assertThat(delivery.getDeliveryNumber()).isEqualTo(68L);
    assertThat(delivery.getDeliveryStatus()).isEqualTo("In Progress");
    assertThat(delivery.getDispatcherName()).isEqualTo("Jane Doe");
    assertThat(delivery.getDispatcherPhoneNumber()).isEqualTo("555.555.5555");
    assertThat(delivery.getDriverName()).isEqualTo("driver");
    assertThat(delivery.getDriverPhoneNumber()).isEqualTo("333.333.3333");
    assertThat(delivery.getToSite()).isEqualTo("site1");
    assertThat(delivery.getFromSite()).isEqualTo("site2");
    assertThat(delivery.getItemList()).containsExactly("water");
    assertThat(delivery.getDriverLicensePlate()).isEqualTo("Traveler");
    assertThat(delivery.getDeliveryDate()).isEqualTo("2024-12-15");
  }

  @Test
  void storeDeliveryWithSitesNotInLocalDatabase() {
    var input = TestDataFile.DELIVERY_DATA_JSON.readData();

    var response = deliveryUpdateWebhook.upsertDelivery(input);
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    var update = DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, "HHHH").orElseThrow();

    assertThat(update.getDeliveryNumber()).isEqualTo(5);
    assertThat(update.getItemList())
        .contains(
            "Buddy heater adapter hose",
            "Toilet Paper",
            "Propane (20lb)",
            "Propane (1lb)",
            "Paper Towels",
            "Kid friendly snacks",
            "Gas Cans",
            "Flashlights",
            "Dog Food",
            "Dish Soap",
            "Cookware",
            "Bedding",
            "Baby Items");

    assertThat(update.getDriverPhoneNumber()).isEqualTo("(444) 333-7022");
    assertThat(update.getDriverName()).isEqualTo("Jason");
    assertThat(update.getDispatcherPhoneNumber()).isEqualTo("919.000.3344");
    assertThat(update.getDispatcherName()).isEqualTo("Dan");
    assertThat(update.getDeliveryStatus()).isEqualTo("Delivery Completed");
    assertThat(update.getDeliveryDate()).isEqualTo("2024-12-13");
    assertThat(update.getDriverLicensePlate()).isNull();
    assertThat(update.getToSiteLink()).isNull();
    assertThat(update.getFromSiteLink()).isNull();

    assertThat(update.getFromSite()).isEqualTo("Valley Hope Foundation");
    assertThat(update.getFromContactName()).isNull();
    assertThat(update.getFromContactPhoneNumber()).isEqualTo("(888) 333-0000");
    assertThat(update.getFromHours()).isEqualTo("Monday - Friday \n10am - 3pm");
    assertThat(update.getFromAddress()).isEqualTo("1035 I40");
    assertThat(update.getFromCity()).isEqualTo("Black Mountain");
    assertThat(update.getFromState()).isEqualTo("NC");

    assertThat(update.getToSite()).isEqualTo("Hope");
    assertThat(update.getToContactName()).isEqualTo("dropoff contact");
    assertThat(update.getToContactPhoneNumber()).isEqualTo("(888) 222-4444");
    assertThat(update.getToHours()).isEqualTo("Monday - Friday \n10am - 5pm");
    assertThat(update.getToAddress()).isEqualTo("60 Flat");
    assertThat(update.getToCity()).isEqualTo("Elk Park");
    assertThat(update.getToState()).isEqualTo("NC");
  }
}
