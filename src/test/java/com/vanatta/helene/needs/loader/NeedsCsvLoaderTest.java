package com.vanatta.helene.needs.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class NeedsCsvLoaderTest {

  @Test
  void load() {
    List<CsvDistroData> data = NeedsCsvLoader.readFile("/test_helene_list.csv");
    assertThat(data).hasSize(5);

    // each data field of the first row we expect to be populated
    var firstData = data.getFirst();
    assertThat(firstData.getOrganizationName()).isNotNull();
    assertThat(firstData.getDonationCenterStatus()).isNotNull();
    assertThat(firstData.getStreetAddress()).isNotNull();
    assertThat(firstData.getCity()).isNotNull();
    assertThat(firstData.getState()).isNotNull();
    assertThat(firstData.getZipCode()).isNotNull();
//    assertThat(firstData.getOtherItemsNotListed()).isNotNull();
    assertThat(firstData.getWinterGear()).isNotNull();
    assertThat(firstData.getAnimalSupplies()).isNotNull();
    assertThat(firstData.getAppliances()).isNotNull();
    assertThat(firstData.getBabyItems()).isNotNull();
    assertThat(firstData.getCleaningSupplies()).isNotNull();
    assertThat(firstData.getCleanup()).isNotNull();
    assertThat(firstData.getClothing()).isNotNull();
    assertThat(firstData.getEmergencyItems()).isNotNull();
    assertThat(firstData.getEquipment()).isNotNull();
    assertThat(firstData.getFirstAid()).isNotNull();
    assertThat(firstData.getFood()).isNotNull();
    assertThat(firstData.getFuelOil()).isNotNull();
    assertThat(firstData.getHydration()).isNotNull();
    assertThat(firstData.getKidsToys()).isNotNull();
    assertThat(firstData.getLinen()).isNotNull();
    assertThat(firstData.getMedsAdult()).isNotNull();
    assertThat(firstData.getOtcMisc()).isNotNull();
    assertThat(firstData.getPaperProducts()).isNotNull();
    assertThat(firstData.getToiletries()).isNotNull();

    // each row should have an organization name, make sure we get that parsed.
    data.forEach(value -> assertThat(value.getOrganizationName()).isNotNull());
  }
}
