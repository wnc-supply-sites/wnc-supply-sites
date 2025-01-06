package com.vanatta.helene.supplies.database.browse.routes;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class BrowseRoutesControllerTest {

  /** Simple check that the browse routes page renders with all of its parameters. */
  @Test
  void validatePageRenders() {
    var controller = new BrowseRoutesController(TestConfiguration.jdbiTest, "");

    ModelAndView modelAndView = controller.browseRoutes(null, null);

    assertThat(modelAndView.getViewName()).isEqualTo("browse/routes");

    Arrays.stream(BrowseRoutesController.TemplateParams.values())
        .forEach(
            param -> assertThat(modelAndView.getModelMap().getAttribute(param.name())).isNotNull());
  }
}
