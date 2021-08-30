package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;

/**
 * A DTO representing a GlobalMessageEvent
 */
@Getter
@ToString
public class GlobalMessageEventDTO extends EventDTO {
    private String message;

    public GlobalMessageEventDTO(GlobalMessageEvent event) {
        super(event);
        this.message = event.getGlobalMessage();
    }
}
