package com.vanatta.helene.supplies.database.supplies.site.details;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SiteDetailTest {

  /**
   * Validate that website link properly strips 'http://' from readable display, and prepends 'http'
   * properly for href.
   */
  @Nested
  class WebsiteLink {

    @Test
    void simpleWebsiteLink() {
      String input = "link.com";
      var output = new SiteDetailController.WebsiteLink(input);
      assertThat(output.getHref()).isEqualTo("http://link.com");
      assertThat(output.getTitle()).isEqualTo("link.com");
    }

    @Test
    void websiteLink() {
      String input = "www.link.com";
      var output = new SiteDetailController.WebsiteLink(input);
      assertThat(output.getHref()).isEqualTo("http://www.link.com");
      assertThat(output.getTitle()).isEqualTo("www.link.com");
    }

    @Test
    void websiteLinkWithHttp() {
      String input = "http://www.link.com";
      var output = new SiteDetailController.WebsiteLink(input);
      assertThat(output.getHref()).isEqualTo("http://www.link.com");
      assertThat(output.getTitle()).isEqualTo("www.link.com");
    }

    @Test
    void websiteLinkWithHttps() {
      String input = "https://www.link.com";
      var output = new SiteDetailController.WebsiteLink(input);
      assertThat(output.getHref()).isEqualTo("https://www.link.com");
      assertThat(output.getTitle()).isEqualTo("www.link.com");
    }

    @Test
    void stripsTrailingSlash() {
      String input = "www.link.com/";
      var output = new SiteDetailController.WebsiteLink(input);
      assertThat(output.getHref()).isEqualTo("http://www.link.com");
      assertThat(output.getTitle()).isEqualTo("www.link.com");
    }
  }

  @Nested
  class ContactHref {

    @Test
    void phoneNumber() {
      String input = "555-555-5555";
      var output = SiteDetailController.ContactHref.newTelephone(input);
      assertThat(output.getHref()).isEqualTo("tel:555-555-5555");
      assertThat(output.getTitle()).isEqualTo("555-555-5555");
    }
  }
}
