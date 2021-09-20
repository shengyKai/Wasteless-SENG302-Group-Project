package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.InterestPurchasedEventDTO;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Represents the event that is sent to users who liked the item that the item has been purchased and is not available
 * anymore.
 */
@Entity
public class InterestPurchasedEvent extends Event{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bought_sale_item_id", nullable = false)
    private BoughtSaleItem boughtSaleItem;

    /**
     * Creates a new InterestPurchasedEvent
     * @param notifiedUser The User who liked the event and needs to be notified about the purchase
     * @param boughtSaleItem The bought Sale item
     */
    public InterestPurchasedEvent(User notifiedUser, BoughtSaleItem boughtSaleItem) {
        super(notifiedUser);
        this.boughtSaleItem = boughtSaleItem;
    }

    public BoughtSaleItem getBoughtSaleItem() { return boughtSaleItem; }

    /**
     * Constructor for JPA
     */
    public InterestPurchasedEvent() {}

    @Override
    public EventDTO asDTO() {
        return new InterestPurchasedEventDTO(this);
    }
}
