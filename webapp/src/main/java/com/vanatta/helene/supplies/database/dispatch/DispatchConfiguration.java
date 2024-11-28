package com.vanatta.helene.supplies.database.dispatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatchConfiguration {


  @Bean
  SendDispatchRequest sendDispatchRequest(@Value("${make.webhook.dispatch.new}") String webhook) {
    return new SendDispatchRequest(webhook);
  }


}
