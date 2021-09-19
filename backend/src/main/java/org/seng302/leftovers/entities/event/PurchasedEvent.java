package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Represents the event that is sent to users who purchase a sale item
 */
@Entity
public class PurchasedEvent extends Event{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="brought_sale_item_id", nullable = false)
    private BoughtSaleItem boughtSaleItem;

    /**
     * Creates a new PurchasedEvent
     * @param purchaser The User who purchased the sale item
     * @param boughtSaleItem The bought Sale item
     */
    public PurchasedEvent(User purchaser, BoughtSaleItem boughtSaleItem) {
        super(purchaser);
        this.boughtSaleItem = boughtSaleItem;
    }

    public BoughtSaleItem getBoughtSaleItem() {
        return boughtSaleItem;
    }

    /**
     * Constructor for JPA
     */
    public PurchasedEvent(){}

    @Override
    public EventDTO asDTO() {
        return null;
    }
}
