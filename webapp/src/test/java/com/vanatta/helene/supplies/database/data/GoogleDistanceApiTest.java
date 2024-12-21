package com.vanatta.helene.supplies.database.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

class GoogleDistanceApiTest {

  // note response value for distance is in meters & response value for time is in seconds
  private static final String sampleResponse =
      """
{
   "destination_addresses" :\s
   [
      "Flat Rock, NC 28731, USA"
   ],
   "origin_addresses" :\s
   [
      "Mills River, NC 28759, USA"
   ],
   "rows" :\s
   [
      {
         "elements" :\s
         [
            {
               "distance" :\s
               {
                  "text" : "18.8 mi",
                  "value" : 30217
               },
               "duration" :\s
               {
                  "text" : "28 mins",
                  "value" : 1656
               },
               "status" : "OK"
            }
         ]
      }
   ]
 }
  """;

  private static final String invalidAddressResponse =
      """
      {
         "destination_addresses" :\s
         [
            ""
         ],
         "origin_addresses" :\s
         [
            "Mills River, NC 28759, USA"
         ],
         "rows" :\s
         [
            {
               "elements" :\s
               [
                  {
                     "status" : "NOT_FOUND"
                  }
               ]
            }
         ],
         "status" : "OK"
      }
      """;

  /**
   * Just validate that we can parse a google distance API response and return correct distance and
   * time values
   */
  @Test
  void responseParsing() {
    var result =
        new Gson().fromJson(sampleResponse, GoogleDistanceApi.GoogleDistanceJson.class);
    assertThat(result.isValid()).isTrue();
    assertThat(result.getDistance()).isEqualTo(18.8);
    assertThat(result.getDuration()).isEqualTo(1656L);
  }
  
  @Test
  void invalidAddressResponseParsing() {
    var result =
        new Gson().fromJson(invalidAddressResponse, GoogleDistanceApi.GoogleDistanceJson.class);
    assertThat(result.isValid()).isFalse();
    assertThat(result.getDistance()).isNull();
    assertThat(result.getDuration()).isNull();
  }
}
