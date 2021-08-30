package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.User;
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
    List<Event> getAllByNotifiedUserOrderByCreated(User user);
}
