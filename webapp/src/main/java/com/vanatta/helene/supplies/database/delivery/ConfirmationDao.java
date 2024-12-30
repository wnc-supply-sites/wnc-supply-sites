package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.util.SecretCodeGenerator;
import java.util.Arrays;
import org.jdbi.v3.core.Jdbi;

/**
 * DAO specifically for updating the confirmation status of deliveries. In contrast 'DeliveryDao' is
 * more for storing the raw delivery data, while this DAO handles confirmation updates.
 */
class ConfirmationDao {

  public static void dispatcherConfirm(Jdbi jdbi, String publicUrlKey) {
    String insert =
        """
        insert into delivery_confirmation(delivery_id, confirm_type, secret_code)
        values(
            (select id from delivery where public_url_key = :publicUrlKey),
            :confirmType,
            :secretCode
        )
        """;
    Arrays.stream(DeliveryConfirmation.ConfirmRole.values())
        .forEach(
            role ->
                jdbi.withHandle(
                    handle ->
                        handle
                            .createUpdate(insert)
                            .bind("publicUrlKey", publicUrlKey)
                            .bind("confirmType", role.name())
                            .bind("secretCode", SecretCodeGenerator.generateCode())
                            .execute()));
  }

  public static void confirmDelivery(
      Jdbi jdbi, String publicUrlKey, DeliveryConfirmation.ConfirmRole confirmRole) {
    updateDeliveryConfirmation(jdbi, publicUrlKey, confirmRole, true);
  }

  public static void cancelDelivery(
      Jdbi jdbi, String publicUrlKey, DeliveryConfirmation.ConfirmRole confirmRole) {
    updateDeliveryConfirmation(jdbi, publicUrlKey, confirmRole, false);
  }

  private static void updateDeliveryConfirmation(
      Jdbi jdbi, String publicUrlKey, DeliveryConfirmation.ConfirmRole confirmRole, boolean value) {

    String update =
        """
        update delivery_confirmation
          set delivery_accepted = :value
        where
          confirm_type = :confirmRole
          and delivery_id = (select id from delivery where public_url_key = :publicUrlKey)
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("value", value)
                .bind("confirmRole", confirmRole.name())
                .bind("publicUrlKey", publicUrlKey)
                .execute());
  }

  public static void updateDriverStatus(
      Jdbi jdbi, String deliveryPublicKey, DriverStatus driverStatus) {

    String update =
        """
        update delivery
        set driver_status = :driverStatus
        where public_url_key = :deliveryPublicKey
        """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("deliveryPublicKey", deliveryPublicKey)
                .bind("driverStatus", driverStatus.name())
                .execute());
  }
}
