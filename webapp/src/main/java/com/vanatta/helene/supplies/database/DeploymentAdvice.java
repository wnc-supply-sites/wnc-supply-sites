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
      log.error("Error looking up short name for domain: {}", domain, e);
      throw e;
    }
  }

  @ModelAttribute(DEPLOYMENT_STATE_LIST)
  public List<String> stateList(HttpServletRequest request) {
    return fetchStateListForHost(jdbi, hostNameLookup.lookupHostName(request));
  }

  static List<String> fetchStateListForHost(Jdbi jdbi, String domain) {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
              select state
              from deployment_states
              where deployment_id = (select id from deployment where lower(domain) = :domain)
            """)
                .bind("domain", domain)
                .mapTo(String.class)
                .list());
  }
}
