package com.vanatta.helene.supplies.database.site.details;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class SiteDetailsResponse {
  String siteName;
  String contactNumber;
  String addressLine1;
  String addressLine2;
  String googleMapsAddress;
}
