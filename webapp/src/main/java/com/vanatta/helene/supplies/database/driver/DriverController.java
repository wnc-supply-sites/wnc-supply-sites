package com.vanatta.helene.supplies.database.driver;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@AllArgsConstructor
public class DriverController {

  private final Jdbi jdbi;
  private final SendDriverUpdate sendDriverUpdate;

  enum PageParams {
    location,
    licensePlates,
    availability,
    comments,
    active,
    canLift50lbs,
    palletCapacity,
    ;
  }

  @GetMapping("/driver/portal")
  ModelAndView showDriverPortal(@ModelAttribute(LoggedInAdvice.USER_PHONE) String userPhone) {
    Driver driver =
        Optional.ofNullable(userPhone)
            .flatMap(phone -> DriverDao.lookupByPhone(jdbi, phone))
            .orElse(null);
    if (driver == null) {
      log.warn("DriverController could not find driver with phone number: {}", userPhone);
      return new ModelAndView("redirect:/");
    }

    System.out.println(driver);

    Map<String, Object> params = new HashMap<>();
    params.put(PageParams.location.name(), Optional.ofNullable(driver.getLocation()).orElse(""));
    params.put(
        PageParams.licensePlates.name(), Optional.ofNullable(driver.getLicensePlates()).orElse(""));
    params.put(
        PageParams.availability.name(), Optional.ofNullable(driver.getAvailability()).orElse(""));
    params.put(PageParams.comments.name(), Optional.ofNullable(driver.getComments()).orElse(""));
    params.put(PageParams.active.name(), driver.isActive());
    params.put(PageParams.canLift50lbs.name(),driver.isCan_lift_50lbs());
    params.put(PageParams.palletCapacity.name(), driver.getPallet_capacity());

    return new ModelAndView("driver/portal", params);
  }

  @PostMapping("/driver/update")
  ResponseEntity<String> updateDriver(
      @ModelAttribute(LoggedInAdvice.USER_PHONE) String userPhone,
      @RequestBody Map<String, String> update) {
    var updatedDriverData =
        DriverDao.lookupByPhone(jdbi, userPhone)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Invalid driver, not found in database. Unable to update: " + userPhone))
            .toBuilder()
            .location(update.get(PageParams.location.name()).trim())
            .licensePlates(update.get(PageParams.licensePlates.name()).trim())
            .availability(update.get(PageParams.availability.name()).trim())
            .comments(update.get(PageParams.comments.name()).trim())
            .can_lift_50lbs(Boolean.parseBoolean(update.get(PageParams.canLift50lbs.name())))
            .pallet_capacity(Integer.parseInt(update.get(PageParams.palletCapacity.name().trim())))
            .build();

    DriverDao.upsert(jdbi, updatedDriverData);
    sendDriverUpdate.sendUpdate(updatedDriverData);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/driver/toggle-active")
  ModelAndView changeDriverActiveStatus(
      @ModelAttribute(LoggedInAdvice.USER_PHONE) String userPhone) {
    DriverDao.toggleActiveStatus(jdbi, userPhone);

    var updatedDriverData =
        DriverDao.lookupByPhone(jdbi, userPhone)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Unexpected, could not find driver in database: " + userPhone));
    sendDriverUpdate.sendUpdate(updatedDriverData);

    return new ModelAndView("redirect:/driver/portal");
  }
}
