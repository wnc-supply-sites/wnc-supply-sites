package com.vanatta.helene.supplies.database.export;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.AllArgsConstructor;

/** Sends updates to 'make' that a new item was created */
@AllArgsConstructor
public class NewItemUpdate {
  private final String webhookUrl;

  public void sendNewItem(String itemName) {
    new Thread(
            () -> HttpPostSender.sendWithJson(webhookUrl, "{\"item-name\":\"" + itemName + "\"}"))
        .start();
  }
}
