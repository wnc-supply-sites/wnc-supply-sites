package com.vanatta.helene.supplies.database.twilio.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsSender {

  // @VisibleForTesting
  public static SmsSender newDisabled(Jdbi jdbi) {
    return new SmsSender("+1", "", "", false, jdbi);
  }

  private final String twilioFromNumber;
  private final boolean twilioSmsEnabled;
  private final Jdbi jdbi;

  SmsSender(
      @Value("${twilio.from.number}") String twilioFromNumber,
      @Value("${twilio.account.sid}") String twilioAccountSid,
      @Value("${twilio.auth.token}") String twilioAuthToken,
      @Value("${twilio.sms.enabled}") boolean twilioSmsEnabled,
      Jdbi jdbi) {
    this.twilioFromNumber = twilioFromNumber;
    if (!twilioFromNumber.startsWith("+1")) {
      throw new IllegalArgumentException(
          "Twilio from number must start with '+1', number provided: " + twilioFromNumber);
    }
    this.twilioSmsEnabled = twilioSmsEnabled;
    if (twilioSmsEnabled) {
      Twilio.init(twilioAccountSid, twilioAuthToken);
    }
    this.jdbi = jdbi;
  }

  public void send(String phoneNumber, String message) {
    if (phoneNumber == null || message == null) {
      throw new IllegalArgumentException(
          String.format("Null input, phoneNumber: %s, message: %s", phoneNumber, message));
    }

    if (!twilioSmsEnabled) {
      log.info("SMS disabled, would have sent to: {}, message: {}", phoneNumber, message);
      recordMessage(
          jdbi,
          MessageResult.builder()
              .toNumber(phoneNumber)
              .messageLength(message.length())
              .messageLink("/fake/uri.json")
              .success(false)
              .errorCode(-1)
              .errorMessage("SMS not enabled")
              .build());
    } else {
      log.info("Sending SMS to: {}, message length: {}", phoneNumber, message.length());

      Message smsMessage =
          Message.creator(
                  new PhoneNumber(phoneNumber.startsWith("+1") ? phoneNumber : "+1" + phoneNumber),
                  new PhoneNumber(twilioFromNumber),
                  message)
              .create();
      recordMessage(jdbi, new MessageResult(smsMessage, message.length()));
    }
  }

  @Builder
  @AllArgsConstructor
  @lombok.Value
  static class MessageResult {
    String toNumber;
    int messageLength;
    boolean success;
    String messageLink;
    Integer errorCode;
    String errorMessage;

    MessageResult(Message smsMessage, int messageLength) {
      toNumber = smsMessage.getTo();
      this.messageLength = messageLength;
      success = smsMessage.getErrorCode() == null;
      messageLink = smsMessage.getUri();
      errorCode = smsMessage.getErrorCode();
      errorMessage = smsMessage.getErrorMessage();
    }
  }

  // @VisibleForTesting
  static void recordMessage(Jdbi jdbi, MessageResult result) {
    String insert =
        """
        insert into sms_send_history(number, message_length, success, message_link, error_code, error_message)
        values(:number, :messageLength, :success, :messageLink, :errorCode, :errorMessage)
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("number", result.getToNumber())
                .bind("messageLength", result.getMessageLength())
                .bind("success", result.isSuccess())
                .bind("messageLink", result.getMessageLink())
                .bind("errorCode", result.getErrorCode())
                .bind("errorMessage", result.getErrorMessage())
                .execute());
  }
}
