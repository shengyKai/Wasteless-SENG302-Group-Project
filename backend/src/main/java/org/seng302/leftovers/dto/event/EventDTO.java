package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.event.Event;

import javax.validation.constraints.NotNull;
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
    private Tag tag;

    protected EventDTO(Event event) {
        this.id = event.getId();
        this.type = event.getClass().getSimpleName();
        this.created = event.getCreated();
        this.tag = event.getTag();
    }
}
