package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.event.DeleteEvent;

/**
 * A DTO representing a DeleteEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class DeleteEventDTO extends EventDTO {
    private String title;
    private MarketplaceCard.Section section;

    /**
     * Converts a DeleteEvent entity to its JSON form
     * @param event DeleteEvent to serialise
     */
    public DeleteEventDTO(DeleteEvent event) {
        super(event);
        this.title = event.getTitle();
        this.section = event.getSection();
    }
}
