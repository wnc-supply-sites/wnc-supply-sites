package com.vanatta.helene.supply.loader;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupplyCsvReaderTest {

  @Test
  void readCsv() {

    List<SupplyData> data = SupplyCsvReader.read("/test-supplies-helene-list.csv");
    assertThat(data).hasSize(4);

    assertThat(data.getFirst().getSiteName()).isEqualTo("Jimmy & Jeans");
    assertThat(data.getFirst().getItems()).isEqualTo("adult diaper, diapers, wipes, clothing");

    data.forEach(value -> assertThat(value.getSiteName()).isNotNull());
    data.forEach(value -> assertThat(value.getItems()).isNotNull());
  }
}
