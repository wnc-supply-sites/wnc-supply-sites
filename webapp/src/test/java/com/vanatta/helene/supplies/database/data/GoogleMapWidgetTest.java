package com.vanatta.helene.supplies.database.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoogleMapWidgetTest {

  @Test
  void generateMapRef() {
    var from =
        GoogleMapWidget.SiteAddress.builder()
            .address("123 very good?")
            .city("twin & city")
            .state("city, hills!")
            .build();
    var to =
        GoogleMapWidget.SiteAddress.builder()
            .address("address")
            .city("twin & city")
            .state("NC")
            .build();

    GoogleMapWidget googleMapWidget = new GoogleMapWidget("secretKey");

    String result = googleMapWidget.generateMapSrcRef(from, to);

    String expected =
        """
    https://www.google.com/maps/embed/v1/directions?key=secretKey&\
    origin=123+very+good%3F,twin+%26+city,city%2C+hills%21&destination=address,twin+%26+city,NC\
    """;

    assertThat(result).isEqualTo(expected);
  }
  
  @Test
  void nullValueHandling() {
    var from =
        GoogleMapWidget.SiteAddress.builder()
            .build();
    var to =
        GoogleMapWidget.SiteAddress.builder()
            .build();
    
    GoogleMapWidget googleMapWidget = new GoogleMapWidget("secretKey");
    
    String result = googleMapWidget.generateMapSrcRef(from, to);
    
    String expected =
        """
    https://www.google.com/maps/embed/v1/directions?key=secretKey&\
    origin=,,&destination=,,\
    """;
    
    assertThat(result).isEqualTo(expected);
  }
}
