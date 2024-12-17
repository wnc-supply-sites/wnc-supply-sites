package com.vanatta.helene.supplies.database.data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleMapWidget {

  private final String apiKey;

  public GoogleMapWidget(@Value("${google.maps.api.key}") String apiKey) {
    this.apiKey = apiKey;
  }

  @Builder
  public static class SiteAddress {
    private final String address;
    private final String city;
    private final String state;
  }

  /**
   * Generates a 'src' URL suitable for an iframe
   *
   * <pre>
   *   Docs:
   *   - https://developers.google.com/maps/documentation/embed/embedding-map
   *   - https://developers.google.com/maps/documentation/embed/get-started
   *
   *   <iframe
   *     width="600"
   *     height="450"
   *     style="border:0"
   *     loading="lazy"
   *     allowfullscreen
   *     referrerpolicy="no-referrer-when-downgrade"
   *     src="https://www.google.com/maps/embed/v1/directions
   *           ?key=YOUR_API_KEY
   *           &origin=Oslo+Norway
   *           &destination=Telemark+Norway">
   *    </iframe>
   * </pre>
   */
  public String generateMapSrcRef(SiteAddress from, SiteAddress to) {
    return String.format(
        "https://www.google.com/maps/embed/v1/directions?key=%s"
            + "&origin=%s,%s,%s&destination=%s,%s,%s",
        apiKey,
        encode(from.address),
        encode(from.city),
        encode(from.state),
        encode(to.address),
        encode(to.city),
        encode(to.state));
  }

  private static String encode(String input) {
    if (input == null) {
      return "";
    } else {
      return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
  }
}
