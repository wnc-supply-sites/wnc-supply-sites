package com.vanatta.helene.supplies.database.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class HostNameLookupTest {

  @Test
  void hostLookup() {
    var mockrequest = new MockHttpServletRequest();
    mockrequest.addHeader("host", "www.Domain.com");

    var result = new HostNameLookup(false, "").lookupHostName(mockrequest);

    assertThat(result).isEqualTo("domain.com");
  }
}
