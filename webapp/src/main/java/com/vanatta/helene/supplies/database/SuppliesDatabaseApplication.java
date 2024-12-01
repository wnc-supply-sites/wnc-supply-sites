package com.vanatta.helene.supplies.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SuppliesDatabaseApplication {
  public static void main(String[] args) {
    SpringApplication.run(SuppliesDatabaseApplication.class, args);
  }
}
