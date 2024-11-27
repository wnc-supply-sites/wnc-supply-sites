package com.vanatta.helene.supplies.database.data.export;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NewItemUpdate {
  private final String webhookUrl;

  public void sendNewItem(String itemName) {
    new Thread(
            () -> HttpPostSender.sendWithJson(webhookUrl, "{\"item-name\":\"" + itemName + "\"}"))
        .start();
  }
}
