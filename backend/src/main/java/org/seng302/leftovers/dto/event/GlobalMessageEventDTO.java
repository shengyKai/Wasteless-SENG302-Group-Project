package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;

/**
 * A DTO representing a GlobalMessageEvent
 */
@Getter
@ToString
@EqualsAndHashCode
public class GlobalMessageEventDTO extends EventDTO {
    private String message;

    /**
     * Converts a GlobalMessageEvent entity to its JSON form
     * @param event GlobalMessageEvent to serialise
     */
    public GlobalMessageEventDTO(GlobalMessageEvent event) {
        super(event);
        this.message = event.getGlobalMessage();
    }
}
