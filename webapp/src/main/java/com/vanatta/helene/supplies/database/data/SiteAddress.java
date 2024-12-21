package com.vanatta.helene.supplies.database.data;

import com.vanatta.helene.supplies.database.util.UrlEncode;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SiteAddress {
  @Nonnull private final String address;
  @Nonnull private final String city;
  @Nonnull private final String state;

  public String toEncodedUrlValue() {
    return String.format(
        "%s,%s,%s", UrlEncode.encode(address), UrlEncode.encode(city), UrlEncode.encode(state));
  }
}
