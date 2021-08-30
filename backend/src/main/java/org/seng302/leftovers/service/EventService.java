package org.seng302.leftovers.service;

import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service for linking event creation/updating and sending the events to users
 * Keeps track of all the currently logged in user's event streams
 */
public interface EventService {
    /**
     * Creates an event emitter for the given user
     * @param user User to make emitter for
     * @return Event emitter for the provided user
     */
    SseEmitter createEmitterForUser(User user);


    /**
     * Save/update an event
     * It will also immediately notify connected users of the event
     * This method should be used in place of eventRepository.save
     *
     * @param event Event to update and notify the recipient
     * @return Saved modified event
     */
    <T extends Event> T saveEvent(T event);
}
