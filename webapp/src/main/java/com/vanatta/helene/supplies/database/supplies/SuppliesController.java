package com.vanatta.helene.supplies.database.supplies;

import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteItem;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyResponse.SiteSupplyData;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SuppliesController {

  @CrossOrigin
  @GetMapping(value = "/supplies")
  public SiteSupplyResponse getTestData() {
    return SiteSupplyResponse.builder()
        .resultCount(3)
        .results(
            List.of(
                SiteSupplyData.builder()
                    .site("site1")
                    .county("Wataug")
                    .items(
                        List.of(
                            SiteItem.builder().name("gloves").status("requested").build(),
                            SiteItem.builder().name("diapers").status("requested").build()))
                    .build(),
                SiteSupplyData.builder()
                    .site("site2")
                    .county("Buncombe")
                    .items(
                        List.of(
                            SiteItem.builder().name("Shampoo").status("oversupply").build(),
                            SiteItem.builder().name("gloves").status("oversupply").build(),
                            SiteItem.builder().name("Soap").status("requested").build()))
                    .build(),
                SiteSupplyData.builder()
                    .site("site3")
                    .county("Ashe")
                    .items(List.of(SiteItem.builder().name("Shampoo").status("urgent").build()))
                    .build()))
        .build();
  }
}
