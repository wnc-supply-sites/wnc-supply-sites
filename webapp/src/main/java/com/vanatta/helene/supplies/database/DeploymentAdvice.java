package com.vanatta.helene.supplies.database;

import com.vanatta.helene.supplies.database.data.HostNameLookup;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * A 'deployment' is a different geographical deployment of the software. It is a different region,
 * conceptually a different 'instance.' (except not, we run just one website instance). Each
 * deployment typically will have a unique domain name associated with it.
 *
 * <p>Configures parameters based upon the requested domain. Different deployments are partitioned
 * by the domain name.
 */
@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class DeploymentAdvice {
  public static final String DEPLOYMENT_DOMAIN_NAME = "domainName";
  public static final String DEPLOYMENT_SHORT_NAME = "deploymentShortName";
  public static final String DEPLOYMENT_STATE_LIST = "deploymentStateList";
  public static final String DEPLOYMENT_ID = "deploymentId";
  private final Jdbi jdbi;
  private final HostNameLookup hostNameLookup;

  @ModelAttribute(DEPLOYMENT_DOMAIN_NAME)
  public String domainName(HttpServletRequest request) {
    return hostNameLookup.lookupHostName(request);
  }

  @ModelAttribute(DEPLOYMENT_SHORT_NAME)
  public String shortName(HttpServletRequest request) {
    return getShortNameForHost(jdbi, hostNameLookup.lookupHostName(request));
  }

  // @VisibleForTesting
  static String getShortNameForHost(Jdbi jdbi, String domain) {
    try {
      return jdbi.withHandle(
          h ->
              h.createQuery("select short_name from deployment where lower(domain) = :domain")
                  .bind("domain", domain)
                  .mapTo(String.class)
                  .one());
    } catch (Exception e) {
      log.warn(
          "Unable to lookup short name for domain: {}, if the domain is legit (not an IP address),"
              + "then this is a real problem. Otherwise ignore this error. Error: {}",
          domain,
          e.getMessage());
      throw e;
    }
  }

  @ModelAttribute(DEPLOYMENT_STATE_LIST)
  public List<String> stateList() {
    return fetchStateListForHost(jdbi);
  }

  @ModelAttribute(DEPLOYMENT_ID)
  public Number deploymentId(HttpServletRequest request) {
    return fetchDeploymentId(jdbi, hostNameLookup.lookupHostName(request));
  }

  static Number fetchDeploymentId(Jdbi jdbi, String domain) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
                              select id
                              from deployment
                              where domain = :domain
                            """)
                .bind("domain", domain)
                .mapTo(Integer.class)
                .one());
  }

  /**
   * @deprecated Avoid using this method. This method is a relic from when we had multiple
   *     "deployments"
   */
  static List<String> fetchStateListForHost(Jdbi jdbi) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
              select state
              from deployment_states
              order by state
            """)
                .mapTo(String.class)
                .list());
  }
}
