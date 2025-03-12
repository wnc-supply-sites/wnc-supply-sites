package com.vanatta.helene.supplies.database.volunteer;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import com.vanatta.helene.supplies.database.volunteer.VolunteerController.SiteSelect;

import java.util.List;

@Slf4j
public class VolunteerDao {


  static List<SiteSelect> fetchSiteSelect(Jdbi jdbi, List<String>states) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
            """
                select s.id, s.name, c.name as county, c.state
                from site s
                join county c on c.id = s.county_id
                where
                c.state in (<states>)
                and
                s.publicly_visible
                and
                s.active
                order by lower(s.name)
                """)
                .bindList("states", states)
                .mapToBean(SiteSelect.class)
                .list());
  }



}
