package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemDTO;
import org.seng302.leftovers.entities.event.InterestPurchasedEvent;

/**
 * A DTO representing a InterestPurchasedEventDTO
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class InterestPurchasedEventDTO extends EventDTO {
    private BoughtSaleItemDTO boughtSaleItemDTO;

    public InterestPurchasedEventDTO(InterestPurchasedEvent event) {
        super(event);
        this.boughtSaleItemDTO = new BoughtSaleItemDTO(event.getBoughtSaleItem());
    }

}
