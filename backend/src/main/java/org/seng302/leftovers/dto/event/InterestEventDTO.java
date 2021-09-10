package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.event.InterestEvent;

/**
 * A DTO representing a InterestEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class InterestEventDTO extends EventDTO {
    private JSONObject saleItem;
    private boolean interested;

    /**
     * Converts a InterestEvent entity to its JSON form
     *
     * @param event InterestEvent to serialise
     */
    public InterestEventDTO(InterestEvent event) {
        super(event);
        // TODO When SaleItem DTO is done this needs updating
        this.saleItem = event.getSaleItem().constructJSONObject();
        this.interested = event.getInterested();
    }
}
