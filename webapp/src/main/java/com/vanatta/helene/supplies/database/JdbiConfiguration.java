package com.vanatta.helene.supplies.database;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JdbiConfiguration {

  @Bean
  public Jdbi jdbi(
      @Value("${jdbi.url}") String url,
      @Value("${jdbi.user}") String user,
      @Value("${jdbi.password}") String password) {
    log.info("Using DB url: {}, user: {}", url, user);
    return Jdbi.create(url, user, password).installPlugin(new SqlObjectPlugin());
  }
}
