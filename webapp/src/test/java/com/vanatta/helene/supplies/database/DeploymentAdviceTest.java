package com.vanatta.helene.supplies.database;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

class DeploymentAdviceTest {
  
  @ParameterizedTest
  @CsvSource({
      "wnc-supply-sites.com,WNC",
      "socal-supply-sites.com,SoCal"
  })
  // @VisibleForTesting
  void getShortNameForHost(String input, String output) {
    
    return "";
  }
  
  
  @ParameterizedTest
  @CsvSource({
      "wnc-supply-sites.com,NC,TN",
      "socal-supply-sites.com,SoCal"
  })
  void fetchStateListForHost(String input, String... states) {
    return List.of();
  }
  
  
}
