package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class InterestPurchasedEvent extends Event{
    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private BoughtSaleItem boughtSaleItem;

    public InterestPurchasedEvent(User notifiedUser, BoughtSaleItem boughtSaleItem) {
        super(notifiedUser);
        this.boughtSaleItem = boughtSaleItem;
    }

    public BoughtSaleItem getBoughtSaleItem() {
        return boughtSaleItem;
    }

    public InterestPurchasedEvent() {}

    @Override
    public EventDTO asDTO() {
        return null;
    }
}
