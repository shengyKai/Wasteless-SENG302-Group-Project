package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PurchasedEvent extends Event{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="brought_sale_item_id", nullable = false)
    private BroughtSaleItem broughtSaleItem;

    public PurchasedEvent(User purchaser, BroughtSaleItem broughtSaleItem) {
        super(purchaser);
        this.broughtSaleItem = broughtSaleItem;
    }

    public BroughtSaleItem getBroughtSaleItem() {
        return broughtSaleItem;
    }

    public PurchasedEvent(){}

    @Override
    public EventDTO asDTO() {
        return null;
    }
}
