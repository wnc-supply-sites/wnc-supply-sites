package com.vanatta.helene.supplies.database.data;

import com.vanatta.helene.supplies.database.delivery.Delivery;
import com.vanatta.helene.supplies.database.util.DateTimeFormat;
import com.vanatta.helene.supplies.database.util.HttpGetSender;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleDistanceApi {
  private final String apiKey;
  private final Supplier<LocalDateTime> timeSupplier;

  private static final String googleMapsApiUrl =
      "https://maps.googleapis.com/maps/api/distancematrix/json";

  // @VisibleForTesting
  public static GoogleDistanceApi stubbed() {
    return new GoogleDistanceApi("") {
      @Override
      public GoogleDistanceResponse queryDistance(SiteAddress from, SiteAddress to) {
        return GoogleDistanceResponse.builder().distance(20.0).duration(320L).build();
      }
    };
  }

  @Autowired
  public GoogleDistanceApi(@Value("${google.maps.api.key}") String apiKey) {
    this.apiKey = apiKey;
    this.timeSupplier = () -> LocalDateTime.now(ZoneId.of("America/New_York"));
  }

  // @VisibleForTesting
  public GoogleDistanceApi(
      @Value("${google.maps.api.key}") String apiKey, Supplier<LocalDateTime> timeSupplier) {
    this.apiKey = apiKey;
    this.timeSupplier = timeSupplier;
  }

  public GoogleDistanceResponse queryDistance(SiteAddress from, SiteAddress to) {
    Map<String, String> params =
        Map.of(
            "key",
            apiKey,
            "origins",
            from.toEncodedUrlValue(),
            "destinations",
            to.toEncodedUrlValue());

    GoogleDistanceJson json =
        HttpGetSender.sendRequest(googleMapsApiUrl, params, GoogleDistanceJson.class);
    return GoogleDistanceResponse.builder()
        .duration(json.getDuration())
        .distance(json.getDistance())
        .valid(json.isValid())
        .build();
  }

  public String estimateEta(Delivery delivery) {
    var from =
        SiteAddress.builder()
            .address(delivery.getFromAddress())
            .city(delivery.getFromCity())
            .state(delivery.getFromState())
            .build();
    var to =
        SiteAddress.builder()
            .address(delivery.getToAddress())
            .city(delivery.getToCity())
            .state(delivery.getToState())
            .build();
    long seconds = queryDistance(from, to).duration;
    var now = timeSupplier.get();
    now = now.plusSeconds(seconds);
    return DateTimeFormat.formatTime(now);
  }

  @Builder
  @lombok.Value
  public static class GoogleDistanceResponse {
    Long duration;
    Double distance;
    boolean valid;
  }

  public static class GoogleDistanceJson {
    DistanceMatrixRow[] rows;

    static class DistanceMatrixRow {
      DistanceMatrixElement[] elements;

      static class DistanceMatrixElement {
        Distance distance;
        Duration duration;
        String status;

        static class Distance {
          long value;
        }

        static class Duration {
          long value;
        }
      }
    }

    boolean isValid() {
      return "OK".equalsIgnoreCase(rows[0].elements[0].status);
    }

    Double getDistance() {
      if (rows.length == 0
          || rows[0].elements.length == 0
          || rows[0].elements[0].distance == null) {
        return null;
      }
      long meters = rows[0].elements[0].distance.value;
      double miles = meters / 1609.34;
      // round to the nearest tenth
      return Math.round(miles * 10) / 10.0;
    }

    // returns time duration in seconds
    Long getDuration() {
      if (rows.length == 0
          || rows[0].elements.length == 0
          || rows[0].elements[0].duration == null) {
        return null;
      }
      return rows[0].elements[0].duration.value;
    }
  }
}
