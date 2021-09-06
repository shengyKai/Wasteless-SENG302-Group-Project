package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.event.Event;

import java.time.Instant;

/**
 * A DTO representing an event
 * Note that this class is abstract, corresponding with Event also being abstract
 */
@Getter
@ToString
public abstract class EventDTO {
    private Long id;
    private String type;
    private Instant created;
    private EventTag tag;
    private EventStatus status;
    private boolean isRead;

    /**
     * Converts a Event entity to its JSON form
     * Adds all the fields common to all Event entities
     * @param event Event to serialise
     */
    protected EventDTO(Event event) {
        this.id = event.getId();
        this.type = event.getClass().getSimpleName();
        this.created = event.getCreated();
        this.tag = event.getTag();
        this.status = event.getStatus();
        // Lombok seems to have converted the "isRead" attribute to "read", not sure how to bypass this but it should
        // be fine with that naming either way
        this.isRead = event.isRead();
    }
}
