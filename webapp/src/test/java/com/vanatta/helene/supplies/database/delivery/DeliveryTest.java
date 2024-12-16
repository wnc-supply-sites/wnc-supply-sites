package com.vanatta.helene.supplies.database.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DeliveryTest {

  @Test
  void itemListTruncated() {
    // item list should display just three items in sorted order.
    // If there are more, then dots ...
    var result = Delivery.getItemListTruncated(null);
    assertThat(result).isEqualTo("");

    result = Delivery.getItemListTruncated(List.of());
    assertThat(result).isEqualTo("");

    result = Delivery.getItemListTruncated(List.of("one", "water", "gloves"));
    assertThat(result).isEqualTo("gloves, one, water");

    result = Delivery.getItemListTruncated(List.of("one", "water", "gloves", "four"));
    assertThat(result).isEqualTo("four, gloves, one...");
  }
}
