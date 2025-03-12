package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.volunteer.VolunteerController.Site;
import java.util.List;

@Slf4j
public class VolunteerDao {


    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
            """
                from site s
                join county c on c.id = s.county_id
                where
                c.state in (<states>)
                and
                and
                order by lower(s.name)
                """)
                .bindList("states", states)
                .list());
  }

}
