package com.vanatta.helene.supplies.database.jobs.distance;

import com.vanatta.helene.supplies.database.data.GoogleDistanceApi;
import com.vanatta.helene.supplies.database.data.SiteAddress;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs periodically, looks for site-site links that have no distance. */
@Slf4j
@Component
public class DistanceCalculator {
  private static final int EVERY_MINUTE_IN_MS = 3 * 60 * 1000;
  private final Jdbi jdbi;
  private final GoogleDistanceApi googleDistanceApi;
  private final boolean enabled;
  private final long delayBetweenRequestsInMs;

  DistanceCalculator(
      Jdbi jdbi,
      GoogleDistanceApi googleDistanceApi,
      @Value("${distance.calculator.enabled}") boolean enabled,
      @Value("${distance.calculator.delay.ms}") int delayBetweenRequestsInMs) {
    this.jdbi = jdbi;
    this.googleDistanceApi = googleDistanceApi;
    this.enabled = enabled;
    this.delayBetweenRequestsInMs = delayBetweenRequestsInMs;
  }

  @Scheduled(fixedDelay = EVERY_MINUTE_IN_MS)
  public void calculateDistances() {
    if (!enabled) {
      return;
    }

    List<DistanceDao.SitePair> sitePairs = DistanceDao.fetchUncalculatedPairs(jdbi);
    if (!sitePairs.isEmpty()) {
      log.info("Distance calculator is computing: {} distances", sitePairs.size());
    }

    for (DistanceDao.SitePair sitePair : sitePairs) {
      SiteDetailDao.SiteDetailData from = SiteDetailDao.lookupSiteById(jdbi, sitePair.getSiteId1());
      SiteDetailDao.SiteDetailData to = SiteDetailDao.lookupSiteById(jdbi, sitePair.getSiteId2());

      var fromAddress =
          SiteAddress.builder()
              .address(from.getAddress())
              .city(from.getCity())
              .state(from.getState())
              .build();
      var toAddress =
          SiteAddress.builder()
              .address(to.getAddress())
              .city(to.getCity())
              .state(to.getState())
              .build();

      GoogleDistanceApi.GoogleDistanceResponse distanceResponse =
          googleDistanceApi.queryDistance(fromAddress, toAddress);

      log.info(
          "Distance between : {}, and: {}, is: {}",
          from.getSiteName(),
          to.getSiteName(),
          distanceResponse);
      if (distanceResponse.isValid()) {
        DistanceDao.updateDistance(
            jdbi,
            sitePair.getSiteId1(),
            sitePair.getSiteId2(),
            distanceResponse.getDistance(),
            distanceResponse.getDuration());
      } else {
        DistanceDao.updateDistanceInvalid(jdbi, sitePair.getSiteId1(), sitePair.getSiteId2());
      }

      // brief sleep so we can space out the API calls somewhat.
      try {
        Thread.sleep(delayBetweenRequestsInMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Distance calculation process interrupted cleanly, aborting..");
        break;
      }
    }
  }
}
