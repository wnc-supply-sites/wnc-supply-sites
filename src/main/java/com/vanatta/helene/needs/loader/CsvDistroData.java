package com.vanatta.helene.needs.loader;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Distribution site data as parsed from CSV. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CsvDistroData {

  @CsvBindByName(column = "Organization Name", required = true)
  String organizationName;

  @CsvBindByName(column = "Donation Center Status")
  String donationCenterStatus;

  @CsvBindByName(column = "Street Address")
  String streetAddress;

  @CsvBindByName(column = "City")
  String city;

  @CsvBindByName(column = "County")
  String county;

  @CsvBindByName(column = "State")
  String state;

  @CsvBindByName(column = "Zip Code")
  String zipCode;

  // Start Time
  // End Time
  // Days of Operation
  // Phone Number
  // Facility Size
  // Facility Type
  // Facility Accommodations
  // Dropoff Requirements
  // Donation Center Timeline
  // Additional Notes
  // Donation Categories

  //  @CsvBindByName(column = "Other Items Not Listed")
  //  String otherItemsNotListed;

  @CsvBindByName(column = "Winter Gear")
  String winterGear;

  @CsvBindByName(column = "Animal Supplies")
  String animalSupplies;

  @CsvBindByName(column = "Appliances")
  String appliances;

  @CsvBindByName(column = "Baby Items")
  String babyItems;

  @CsvBindByName(column = "Cleaning Supplies")
  String cleaningSupplies;

  @CsvBindByName(column = "Cleanup")
  String cleanup;

  @CsvBindByName(column = "Clothing")
  String clothing;

  @CsvBindByName(column = "Emergency Items")
  String emergencyItems;

  @CsvBindByName(column = "Equipment")
  String equipment;

  @CsvBindByName(column = "First Aid")
  String firstAid;

  @CsvBindByName(column = "Food")
  String food;

  @CsvBindByName(column = "Fuel/Oil")
  String fuelOil;

  @CsvBindByName(column = "Hydration")
  String hydration;

  @CsvBindByName(column = "Kids Toys")
  String kidsToys;

  @CsvBindByName(column = "Linen")
  String linen;

  @CsvBindByName(column = "Meds Adult, OTC")
  String medsAdult;

  @CsvBindByName(column = "Meds Child, OTC")
  String medsChild;

  @CsvBindByName(column = "Misc.")
  String otcMisc;

  @CsvBindByName(column = "Paper Products")
  String paperProducts;

  @CsvBindByName(column = "Toiletries")
  String toiletries;

  public String getOrganizationName() {
    if (organizationName.endsWith("*")) {
      return organizationName.substring(0, organizationName.length() - 1);
    } else {
      return organizationName;
    }
  }

  public String getCounty() {
    if ("Hamblen County".equals(county)) {
      return "Hamblen";
    } else {
      return county;
    }
  }

  public String getClothing() {
    if (clothing == null) {
      return clothing;
    } else {
      return "clothing\n" + clothing;
    }
  }
}
