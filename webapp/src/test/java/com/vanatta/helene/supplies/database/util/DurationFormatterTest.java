package com.vanatta.helene.supplies.database.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DurationFormatterTest {

  @ParameterizedTest
  @CsvSource({
    "0,0 min", //
    "1,1 min",
    "59,59 min",
    "60,1 hr",
    "61,1 hr 1 min",
    "360,6 hr"
  })
  void format(int input, String expected) {
    var inputDuration = Duration.ofMinutes(input);

    String output = DurationFormatter.formatDuration(inputDuration);

    assertThat(output).isEqualTo(expected);
  }
}
