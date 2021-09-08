package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

    /**
     * Finds all the events for the given user where the last date the event was modified is after the given date. The
     * returned events will be in descending order by their creation date.
     * @param user Only events where the notified user is this user will be returned.
     * @param modifiedSince Only events which have been modified after this instant will be returned.
     * @return List of events the user needs to be notified of.
     */
    @Query("select e from Event e where e.notifiedUser = :user and e.lastModified > :modifiedSince order by e.created desc")
    List<Event> findEventsForUser(User user, Instant modifiedSince);

    /**
     * Finds all the events for the given user. The returned events will be in descending order by their creation date.
     * @param user Only events where the notified user is this user will be returned.
     * @return List of events the user needs to be notified of.
     */
    @Query("select e from Event e where e.notifiedUser = :user order by e.created desc")
    List<Event> findEventsForUser(User user);
}
