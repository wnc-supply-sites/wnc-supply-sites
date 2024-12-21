package com.vanatta.helene.supplies.database.data;

import com.vanatta.helene.supplies.database.util.UrlEncode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleMapWidget {

  private final String apiKey;

  public GoogleMapWidget(@Value("${google.maps.api.key}") String apiKey) {
    this.apiKey = apiKey;
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
        encode(from.getAddress()),
        encode(from.getCity()),
        encode(from.getState()),
        encode(to.getAddress()),
        encode(to.getCity()),
        encode(to.getState()));
  }

  private static String encode(String input) {
    return UrlEncode.encode(input);
  }
}
