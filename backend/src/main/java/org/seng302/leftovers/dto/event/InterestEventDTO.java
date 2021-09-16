package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.SaleItemDTO;
import org.seng302.leftovers.entities.event.InterestEvent;

/**
 * A DTO representing a InterestEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class InterestEventDTO extends EventDTO {
    private SaleItemDTO saleItem;
    private boolean interested;

    /**
     * Converts a InterestEvent entity to its JSON form
     *
     * @param event InterestEvent to serialise
     */
    public InterestEventDTO(InterestEvent event) {
        super(event);
        this.saleItem = new SaleItemDTO(event.getSaleItem());
        this.interested = event.getInterested();
    }

    /**
     * Constructor for helping with building this object from a string
     */
    protected InterestEventDTO() {}
}
