package com.vanatta.helene.supplies.database.manage.add.site;

import com.vanatta.helene.supplies.database.data.SiteType;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@AllArgsConstructor
@Value
public class AddSiteData {

  String contactNumber;
  String website;

  @Nonnull SiteType siteType;
  @Nonnull String siteName;
  @Nonnull String streetAddress;
  @Nonnull String city;
  @Nonnull String county;
  @Nonnull String state;
}
