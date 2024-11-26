package com.vanatta.helene.supplies.database.auth;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

  @Bean
  CookieAuthenticator authKey(Jdbi jdbi) {
    return new CookieAuthenticator(jdbi);
  }
}
