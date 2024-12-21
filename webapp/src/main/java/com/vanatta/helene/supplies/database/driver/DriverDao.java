package com.vanatta.helene.supplies.database.driver;


import lombok.Data;

public class DriverDao {
  
  
  @Data
  static class Driver {
    private String fullName;
    private String email;
    private String phone;
    private boolean active;
    private String location;
  }
  
  public static Driver lookupByAirtableId(long id) {
  
  
    throw new UnsupportedOperationException("TODO");
  
  }
}
