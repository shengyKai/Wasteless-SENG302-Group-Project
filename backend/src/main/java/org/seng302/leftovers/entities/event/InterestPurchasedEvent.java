package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;

@Entity
public class InterestPurchasedEvent extends Event{
    private SaleItem saleItem;

    public InterestPurchasedEvent(User notifiedUser, SaleItem saleItem, User buyer) {
        super(notifiedUser);
        this.saleItem = saleItem;
    }

    public InterestPurchasedEvent() {}

    @Override
    public EventDTO asDTO() {
        return null;
    }
}
