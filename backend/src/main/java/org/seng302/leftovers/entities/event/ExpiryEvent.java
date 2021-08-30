package org.seng302.leftovers.entities.event;

import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.event.DeleteEventDTO;
import org.seng302.leftovers.dto.event.ExpiryEventDTO;
import org.seng302.leftovers.entities.MarketplaceCard;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * This class is used for notifying users when their marketplace card is about to expire.
 */
@Entity
public class ExpiryEvent extends Event {

    @OneToOne
    @JoinColumn(name = "expiring_card", unique = true, nullable = false)
    private MarketplaceCard expiringCard;

    protected ExpiryEvent() {} // Required by JPA

    /**
     * Creates a new event for a expiring card
     * @param expiringCard Card that is about to expire
     */
    public ExpiryEvent(MarketplaceCard expiringCard) {
        super(expiringCard.getCreator());
        this.expiringCard = expiringCard;
    }

    /**
     * Returns the marketplace card nearing expiry which is associated with this event.
     * @return The marketplace card associated with this event.
     */
    public MarketplaceCard getExpiringCard() {
        return expiringCard;
    }

    @Override
    public ExpiryEventDTO asDTO() {
        return new ExpiryEventDTO(this);
    }
}
