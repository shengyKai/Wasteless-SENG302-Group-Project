package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemDTO;
import org.seng302.leftovers.entities.event.PurchasedEvent;


/**
 * A DTO representing a PurchasedEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PurchasedEventDTO extends EventDTO{
    private BoughtSaleItemDTO boughtSaleItem;

    /**
     * Converts a PurchasedEvent into its JSON form
     * @param event The PurchasedEvent to serialize
     */
    public PurchasedEventDTO(PurchasedEvent event) {
        super(event);
        this.boughtSaleItem = new BoughtSaleItemDTO(event.getBoughtSaleItem(), true);
    }
}
