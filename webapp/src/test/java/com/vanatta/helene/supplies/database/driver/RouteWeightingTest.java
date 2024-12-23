package com.vanatta.helene.supplies.database.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class RouteWeightingTest {

  @Value
  @Builder
  static class WeightingAlgorithm {
    /** 10, 25, 10 -> relatively optimal weights */
    List<Double> weights;
    @Nonnull Function<String, Double> normalWeight;
    @Nonnull Function<String, Double> urgentWeight;
    @Nonnull Function<Double, Double> distanceWeight;
  }

  static List<WeightingAlgorithm> routeWeighting() {
    List<WeightingAlgorithm> algorithms = new ArrayList<>();
    for (double normal = 10.0; normal <= 13.0; normal += 3.0) {
      for (double urgent = Math.max(normal, 25.0); urgent <= 30.0; urgent += 3.0) {
        for (double distance = 9.0; distance <= 11.0; distance++) {
          final double normalWeight = normal;
          final double urgentWeight = urgent;
          final double distanceWeight = distance;
          algorithms.add(
              WeightingAlgorithm.builder()
                  .weights(List.of(normal, urgent, distance))
                  .normalWeight(itemName -> normalWeight)
                  .urgentWeight(itemName -> urgentWeight)
                  .distanceWeight(d -> Math.pow(d / distanceWeight, 2))
                  .build());
        }
      }
    }
    return algorithms;

    /*
    return List.of(
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 1.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build(),
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 5.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build(),
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 6.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build(),
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 8.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build(),
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 10.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build(),
        WeightingAlgorithm.builder()
            .normalWeight(itemName -> 15.0)
            .urgentWeight(itemName -> 20.0)
            .distanceWeight(distance -> Math.pow(distance / 10.0, 2))
            .build()

        );

     */

  }

  @Disabled
  @ParameterizedTest
  @MethodSource
  void routeWeighting(WeightingAlgorithm algorithm) {
    String deliveryDataJson = TestDataFile.DELIVERY_TRAINING_DATA.readData();

    List<TrainingData.Delivery> deliveries =
        new Gson().fromJson(deliveryDataJson, TrainingData.class).getDeliveries();
    assertThat(deliveries).isNotEmpty();

    int showPassCount = 0;
    int showFailCount = 0;
    int hidePassCount = 0;
    int hideFailCount = 0;

    List<TrainingData.Delivery> hideFails = new ArrayList<>();
    List<TrainingData.Delivery> showFails = new ArrayList<>();

    for (TrainingData.Delivery delivery : deliveries) {
      boolean result =
          RouteWeighting.filter(
              RouteWeighting.RouteData.builder()
                  .distance(delivery.getDistance())
                  .items(
                      delivery.getItems().stream()
                          .map(
                              i ->
                                  RouteWeighting.RouteData.Item.builder()
                                      .name(i.getName())
                                      .priority(i.getPriority())
                                      .build())
                          .toList())
                  .build(),
              algorithm.normalWeight,
              algorithm.urgentWeight,
              algorithm.distanceWeight);

      if (delivery.isShow() && result) {
        showPassCount++;
      } else if (delivery.isShow() && !result) {
        log.warn("Invalid show: {}", delivery);
        showFails.add(delivery);
        showFailCount++;
      } else if (!delivery.isShow() && result) {
        log.warn("Invalid hide: {}", delivery);
        hideFails.add(delivery);
        hideFailCount++;
      } else {
        assert !delivery.isShow() && !result;
        hidePassCount++;
      }
    }

    assertThat(hideFailCount == 0 && showFailCount == 0)
        .describedAs(
            String.format(
                """
              Overall: %s
              Weights: %s
              Show Pass: %s
              Show Fail: %s
              Hide Pass: %s
              Hide Fail: %s

              Show fails: %s

              Hide Fails: %s

              """,
                ((double) (deliveries.size() - (showFailCount + hideFailCount)))
                    / deliveries.size(),
                algorithm.weights,
                showPassCount,
                showFailCount,
                hidePassCount,
                hideFailCount,
                showFails.stream()
                    .map(TrainingData.Delivery::toString)
                    .collect(Collectors.joining("\n")),
                hideFails.stream()
                    .map(TrainingData.Delivery::toString)
                    .collect(Collectors.joining("\n"))))
        .isTrue();
  }

  @Value
  static class TrainingData {
    List<Delivery> deliveries;

    @Value
    static class Delivery {
      boolean show;
      double distance;
      List<Item> items;
    }

    @Value
    static class Item {
      String name;
      String priority;
    }
  }
}
