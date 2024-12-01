package com.vanatta.helene.supplies.database.test.util;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AllArgsConstructor;

/** Can be used to pull data from files in "test/resources" */
@AllArgsConstructor
public enum TestDataFile {
  TEST_DATA_SCHEMA("/TestData.sql"),
  STATUS_CHANGE_JSON("/webhook/json/status-change.json"),
  ;
  private final String path;

  public String readData() {
    try {
      return Files.readString(Path.of(TestDataFile.class.getResource(path).toURI()));
    } catch (Exception e) {
      throw new RuntimeException("Error with data file: " + path, e);
    }
  }
}
