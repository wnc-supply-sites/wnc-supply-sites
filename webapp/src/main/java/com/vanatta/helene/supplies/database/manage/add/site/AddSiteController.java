package com.vanatta.helene.supplies.database.manage.add.site;

import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class AddSiteController {

  private final Jdbi jdbi;
  private final SendSiteUpdate sendSiteUpdate;

  /** Shows the form for adding a brand new site */
  @GetMapping("/manage/new-site/add-site")
  ModelAndView showAddNewSiteForm() {
    Map<String, Object> model = new HashMap<>();

    Map<String, List<String>> counties = CountyDao.fetchFullCountyListing(jdbi);
    model.put("fullCountyList", counties);
    model.put("stateList", ManageSiteController.createItemListing("NC", counties.keySet()));
    model.put(
        "countyList",
        ManageSiteController.createItemListing(counties.get("NC").getFirst(), counties.get("NC")));

    return new ModelAndView("manage/new-site/add-site", model);
  }

  /** REST endpoint to create a new site */
  @PostMapping("/manage/add-site")
  @ResponseBody
  ResponseEntity<?> postNewSite(@RequestBody Map<String, String> params) {
    log.info("Received add new site data: {}", params);
    var addSiteData =
        AddSiteData.builder()
            .contactNumber(params.get("contactNumber"))
            .website(params.get("website"))
            .siteType(SiteType.parseSiteType(params.get("siteType")))
            .siteName(params.get("siteName"))
            .streetAddress(params.get("streetAddress"))
            .city(params.get("city"))
            .county(params.get("county"))
            .state(params.get("state"))
            .build();
    if (addSiteData.isMissingRequiredData()) {
      log.warn(
          "Add new site data is missing required data. Add new site data received: {}",
          addSiteData);
      // front end should be enforcing required data, error messaging back to user here is
      // pretty minimal.
      return ResponseEntity.badRequest().body("Failed, missing required data.");
    }
    try {
      long newSiteId = AddSiteDao.addSite(jdbi, addSiteData);
      sendSiteUpdate.sendFullUpdate(newSiteId);
      return ResponseEntity.ok(
          "{\"result\": \"success\", \"editSiteInventoryUrl\": \"/manage/inventory?siteId="
              + newSiteId
              + "\"}");
    } catch (AddSiteDao.DuplicateSiteException e) {
      return ResponseEntity.badRequest()
          .body("{\"result\": \"fail\", \"error\": \"site name already exists\"}");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(String.format("{\"result\": \"fail\", \"error\": \"%s\"}", e.getMessage()));
    }
  }
}
