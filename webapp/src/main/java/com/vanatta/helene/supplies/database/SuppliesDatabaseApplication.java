package com.vanatta.helene.supplies.database;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.dispatch.DispatchRequestService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@Slf4j
public class SuppliesDatabaseApplication {

  private final Jdbi jdbi;
  private final boolean backfillEnabled;

  SuppliesDatabaseApplication(
      Jdbi jdbi, @Value("${dispatch.request.backfill.enabled}") boolean backfillEnabled) {
    this.jdbi = jdbi;
    this.backfillEnabled = backfillEnabled;
    log.info("Dispatch requests backfill enabled: " + backfillEnabled);
  }

  public static void main(String[] args) {
    SpringApplication.run(SuppliesDatabaseApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void runBackFillAfterStartup() {
    if (!backfillEnabled) {
      return;
    }

    String query =
        """
        select
          s.name siteName,
          i.name itemName,
          its.name itemStatus
        from site_item si
        join site s on s.id = si.site_id
        join item i on i.id = si.item_id
        join item_status its on its.id = si.item_status_id
        where
           its.name in ('Needed', 'Urgently Needed')
           and not exists
           (select 1
            from dispatch_request dr
            where dr.site_id = si.site_id and dr.item_id = si.item_id)
        """;

    var results =
        jdbi.withHandle(
            handle -> handle.createQuery(query).mapToBean(MissingDispatch.class).list());

    results.forEach(
        r -> {
          try {
            log.info("Sending dispatch request backfill: {}", r);

            DispatchRequestService.create(jdbi)
                .computeDispatch(
                    r.getSiteName(), r.getItemName(), ItemStatus.fromTextValue(r.getItemStatus()));
          } catch (Exception e) {
            log.error("Failed to send dispatch request: {}", r, e);
          }
        });
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MissingDispatch {
    String siteName;
    String itemName;
    String itemStatus;
  }
}
