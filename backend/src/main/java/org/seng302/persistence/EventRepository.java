package org.seng302.persistence;

import org.seng302.entities.Event;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    /**
     * Finds all the events for the given user
     * @param user User to find events for
     * @return List of events the user needs to be notified of
     */
    List<Event> getAllByNotifiedUsersOrderByCreated(User user);
}
