package com.vanatta.helene.supplies.database.admin.item;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sends an update to Airtable that items have been merged. In order to avoid complexity, and to
 * avoid updating any deliveries, we will simply send the WSS-IDs of the items and Airtable will run
 * a script to handle the 'merged' items. Airtable will presumably just delete the WSS-ID values
 */
@Slf4j
@Component
public class SendItemMergedUpdate {

  static SendItemMergedUpdate disabled() {
    return new SendItemMergedUpdate(false, "");
  }

  final String airtableWebhookUrl;
  final boolean enabled;

  SendItemMergedUpdate(
      @Value("${make.enabled}") boolean enabled,
      @Value("${airtable.webhook.item.merge}") String webhookUrl) {
    this.airtableWebhookUrl = webhookUrl;
    this.enabled = enabled;
  }

  /**
   * @param deletedItemWssIds The WSS ID of items that were merged into another item. This is the
   *     list of items that were merged and deleted. Note, the WSS-ID is the public ID of the item,
   *     it is not the primary key ID column, but is `item.wss_id`. Airtable does not do anything
   *     with the "Merged-Into Item", so we do not require that value as a parameter.
   */
  void sendMergedItems(List<Long> deletedItemWssIds) {
    if (!enabled) {
      return;
    }
    log.info(
        "Sending to airtable list of items that were merged and are now deleted: {}",
        deletedItemWssIds);
    HttpPostSender.sendJson(
        airtableWebhookUrl,
        String.format(
            """
                {"mergedItemWssIds": %s}
                """,
            deletedItemWssIds));
  }
}
