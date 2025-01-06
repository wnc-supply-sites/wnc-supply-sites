package com.vanatta.helene.supplies.database.route.browser;

import java.util.List;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

public class RouteWeighting {

  @Builder
  @Value
  @AllArgsConstructor
  public static class RouteData {
    double distance;
    List<Item> items;

    RouteData(RouteBrowserDao.DeliveryOption deliveryOption) {
      // if we don't know the distance between two sites.. just use a ballpark of about 100 miles.
      this.distance =
          deliveryOption.getDistanceMiles() == null ? 100.0 : deliveryOption.getDistanceMiles();
      this.items =
          deliveryOption.getItems().stream()
              .map(
                  deliveryItem ->
                      Item.builder()
                          .name(deliveryItem.getName())
                          .priority(
                              deliveryItem.getUrgencyCssClass().equals("urgent")
                                  ? "URGENT"
                                  : "NORMAL")
                          .build())
              .toList();
    }

    @Builder
    @Value
    static class Item {
      String name;
      String priority;
    }
  }

  public static boolean filter(RouteBrowserDao.DeliveryOption deliveryOption) {
    return filter(new RouteData(deliveryOption));
  }

  public static boolean filter(RouteData input) {
    return filter(input, _ -> 10.0, _ -> 25.0, d -> Math.pow(d / 10, 2));
  }

  /**
   * For a given input, applies a weighting algorithm and returs true if the 'route data' passes.
   * False if it does not. False implies the route is "silly", eg: send toothbrushes 300 miles, vs
   * an important route like "send urgently needed electric blankets 20 miles"
   */
  public static boolean filter(
      RouteData input,
      Function<String, Double> normalWeight,
      Function<String, Double> urgentWeight,
      Function<Double, Double> distanceWeight) {
    if (input.distance <= 0.0) {
      return false;
    }

    double itemWeightScore =
        input.getItems().stream()
            .filter(i -> i.getPriority().equals("NORMAL"))
            .map(RouteData.Item::getName)
            .mapToDouble(normalWeight::apply)
            .sum();
    double priorityItemWeightScore =
        input.getItems().stream()
            .filter(i -> i.getPriority().equals("URGENT"))
            .map(RouteData.Item::getName)
            .mapToDouble(urgentWeight::apply)
            .sum();
    double distanceWeightScore = distanceWeight.apply(input.distance);
    return (itemWeightScore + priorityItemWeightScore - distanceWeightScore) > 0;
  }

  /*
  YES
  -----
  72 miles
  Heaters   U
  Kerosene  U



  20.5 miles (26 min)
  Childrens OTC Meds
  Garbage Bags    U

  100.0 miles
  Heaters   U
  Propane


  120 miles
  Heater

  17.9 miles (22 min)
  Water U


  25.0 miles
  Clothing (New)

  1.0 miles (1 min)
  n95 mask


  49.5 miles (1 hr 13 min)
  Motrin & Tylenol U



  NO
  ---

  49.5 miles (1 hr 13 min)
  Motrin & Tylenol


  48.1 miles (1 hr 8 min)
  Disinfectant

  30 miles
  Clothing (New)


  70.9 miles (1 hr 30 min)
  Dish Soap


  30 miles
  Christmas presents (For Teens)
  Food (Non-Perishable)


  200 miles
  Heaters   U
  Kerosene  U

  50 miles (26 min)
  Childrens OTC Meds
  Garbage Bags    U





   */

}
