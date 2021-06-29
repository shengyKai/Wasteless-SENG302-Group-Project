package org.seng302.service;

import org.seng302.entities.Event;
import org.seng302.entities.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

public interface EventService {
    /**
     * Creates an event emitter for the given user
     * @param user User to make emitter for
     * @return Event emitter for the provided user
     */
    SseEmitter createEmitterForUser(User user);

    /**
     * Adds users to an event and notifies them if they are connected.
     * This will also save the event with the users added
     * @param users Set of users to notify
     * @param event Event to notify users of
     * @return Saved modified event
     */
    Event addUsersToEvent(Set<User> users, Event event);

    /**
     * Add a single user to an event and notifies them if they are connected
     * This will also save the event with the users added
     * Convenience wrapper around "addUsersToEvent"
     * @param user User to notify
     * @param event Event to notify users of
     * @return Saved modified event
     */
    default Event addUserToEvent(User user, Event event) {
        return addUsersToEvent(Set.of(user), event);
    }
}
