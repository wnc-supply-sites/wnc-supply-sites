package com.vanatta.helene.supplies.database.data;

import com.vanatta.helene.supplies.database.util.HttpGetSender;
import java.util.Map;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleDistanceApi {
  private final String apiKey;

  private static final String googleMapsApiUrl =
      "https://maps.googleapis.com/maps/api/distancematrix/json";

  public GoogleDistanceApi(@Value("${google.maps.api.key}") String apiKey) {
    this.apiKey = apiKey;
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
