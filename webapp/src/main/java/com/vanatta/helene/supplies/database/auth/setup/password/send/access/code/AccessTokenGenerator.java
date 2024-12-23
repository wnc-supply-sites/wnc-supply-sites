package com.vanatta.helene.supplies.database.auth.setup.password.send.access.code;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenGenerator {
  private final SecureRandom secureRandom = new SecureRandom();

  public String generate() {
    return String.valueOf(secureRandom.nextInt(1_000_000));
  }
}
