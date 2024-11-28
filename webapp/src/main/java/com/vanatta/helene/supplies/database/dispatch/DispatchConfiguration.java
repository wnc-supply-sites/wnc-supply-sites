package com.vanatta.helene.supplies.database.dispatch;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatchConfiguration {


  @Bean
  SendDispatchRequest sendDispatchRequest(
      Jdbi jdbi,
      @Value("${make.webhook.dispatch.new}") String webhook) {
    return new SendDispatchRequest(jdbi, webhook, "", "");
  }


}
