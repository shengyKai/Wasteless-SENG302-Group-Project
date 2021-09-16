package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.SaleItemDTO;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.entities.event.InterestPurchasedEvent;

/**
 * A DTO representing a InterestPurchasedEventDTO
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class InterestPurchasedEventDTO extends EventDTO {
    //TODO may have to create a boughtSaleItemDTO
    private SaleItemDTO saleItem;

    /**
     * Converts a InterestPurchasedEvent entity to its JSON form
     *
     * @param event InterestEvent to serialise
     */
    public InterestPurchasedEventDTO(InterestPurchasedEvent event) {
        super(event);
        this.saleItem = new SaleItemDTO(event.getBoughtSaleItem());
    }

    /**
     * Constructor for helping with building this object from a string
     */
    protected InterestEventDTO() {}
}
