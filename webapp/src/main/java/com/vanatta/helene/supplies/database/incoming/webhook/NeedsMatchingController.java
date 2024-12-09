package com.vanatta.helene.supplies.database.incoming.webhook;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supports a webhook endpoint that can take a 'from' site, 'to' site, then invokes a MAKE webhook
 * to send an update of which supplies can be delivered between the two.
 */
@Slf4j
@RestController
public class NeedsMatchingController {

  private static final String PATH_ADD_NEEDS = "/webhook/add-supplies-to-delivery";
  private final String addToDeliveryWebhook;
  private final Jdbi jdbi;

  NeedsMatchingController(
      Jdbi jdbi, @Value("${make.webhoook.addToDelivery}") String addToDeliveryWebhook) {
    this.jdbi = jdbi;
    this.addToDeliveryWebhook = addToDeliveryWebhook;
  }

  @Builder
  //  @AllArgsConstructor
  @lombok.Value
  public static class ComputedNeeds {
    long deliveryId;
    List<Long> wssIdsNeedList;
  }

  /**
   * Given inputs: [from site, to site, delivery id]<br>
   * Compute which needs can be sent from site to the to site.<br>
   * Invoke make job to update the target delivery id with the computed needs.
   */
  @PostMapping(PATH_ADD_NEEDS)
  ResponseEntity<String> addSuppliesToDelivery(@RequestBody String body) {//Map<String, String> body) {
    log.info("RECEIVED: " + body);
    
//    long deliveryId = Long.parseLong(body.get("deliveryId"));
//    long fromWssId = Long.parseLong(body.get("fromSiteWssId"));
//    long toSiteWssId = Long.parseLong(body.get("toSiteWssId"));
//
//
//
//    List<Long> needsIds = computeNeedsMatch(jdbi, fromWssId, toSiteWssId);
//    log.info("Received needs computation request: {}, matched with needs: {}", body, needsIds);
//
//    if (!needsIds.isEmpty()) {
//      var computedNeed =
//          ComputedNeeds.builder().deliveryId(deliveryId).wssIdsNeedList(needsIds).build();
//      HttpPostSender.sendAsJson(addToDeliveryWebhook, computedNeed);
//    }
//    return ResponseEntity.ok("Matches: " + needsIds.size());
    return ResponseEntity.ok("");
  }
  

  // @VisibleForTesting
  static List<Long> computeNeedsMatch(Jdbi jdbi, long fromSiteWssId, long toSiteWssId) {

    String availableItemsQuery =
        """
        select
          si.item_id
        from site_item si
        join site s on s.id = si.site_id
        join site_type st on st.id = s.site_type_id
        join item_status its on its.id = si.item_status_id
        where s.wss_id = :fromSiteWssId
          and
          (
            ( upper(st.name) = 'SUPPLY HUB' and upper(its.name) in ('AVAILABLE', 'OVERSUPPLY') )
            or
            ( upper(st.name) = 'DISTRIBUTION CENTER' and upper(its.name) = 'OVERSUPPLY' )
          )
        """;
    List<Long> itemIdsAvailable =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(availableItemsQuery)
                    .bind("fromSiteWssId", fromSiteWssId)
                    .mapTo(Long.class)
                    .list());
    if (itemIdsAvailable.isEmpty()) {
      return List.of();
    }

    String neededItemsQuery =
        """
        select
          si.item_id
        from site_item si
        join site s on s.id = si.site_id
        join item_status its on its.id = si.item_status_id
        where s.wss_id = :toSiteWssId
          and upper(its.name) in ('NEEDED', 'URGENTLY NEEDED')
        """;
    List<Long> itemsIdsNeeded =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(neededItemsQuery)
                    .bind("toSiteWssId", toSiteWssId)
                    .mapTo(Long.class)
                    .list());

    List<Long> eligibleItemIds = new ArrayList<>(itemIdsAvailable);
    eligibleItemIds.retainAll(itemsIdsNeeded);

    if (eligibleItemIds.isEmpty()) {
      return List.of();
    }

    String queryNeedIds =
        """
        select
          wss_id
        from site_item
        where
          site_id = (select id from site where wss_id = :siteWssId)
          and item_id in (<itemIds>)
        order by wss_id desc;
        """;
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(queryNeedIds)
                .bind("siteWssId", toSiteWssId)
                .bindList("itemIds", eligibleItemIds)
                .mapTo(Long.class)
                .list());
  }
}
