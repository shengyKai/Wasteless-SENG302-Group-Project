package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.event.DeleteEvent;
import org.seng302.leftovers.entities.event.Event;

import java.time.Instant;

/**
 * A DTO representing a DeleteEvent
 */
@Getter
@ToString
public class DeleteEventDTO extends EventDTO {
    private String title;
    private MarketplaceCard.Section section;

    public DeleteEventDTO(DeleteEvent event) {
        super(event);
        this.title = event.getTitle();
        this.section = event.getSection();
    }
}
