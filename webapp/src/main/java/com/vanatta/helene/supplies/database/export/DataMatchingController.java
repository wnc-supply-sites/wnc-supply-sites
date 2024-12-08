package com.vanatta.helene.supplies.database.export;

import com.vanatta.helene.supplies.database.supplies.site.details.NeedsMatchingDao;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class DataMatchingController {

  private final Jdbi jdbi;

  @GetMapping("/export/needs-matching")
  ModelAndView needsMatching(@RequestParam(required = true) String airtableId) {
    // TODO: validate input & throw bad arg..

    var matchList = NeedsMatchingDao.execute(jdbi, Long.parseLong(airtableId));

    Map<String, Object> pageData = new HashMap<>();
    pageData.put("matchingResults", matchList);
    return new ModelAndView("export/needs-matching", pageData);
  }
}
