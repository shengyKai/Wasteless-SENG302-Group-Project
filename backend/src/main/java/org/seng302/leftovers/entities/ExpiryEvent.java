package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * This class is used for notifying users when their marketplace card is about to expire.
 */
@Entity
public class ExpiryEvent extends Event{

    @OneToOne
    private MarketplaceCard expiringCard;

    protected ExpiryEvent() {

    }

    public ExpiryEvent(MarketplaceCard expiringCard) {
        this.expiringCard = expiringCard;
    }

    /**
     * Construct a JSON representation of the expiry event. Contains the event's id, created date and type, and the JSON
     * representation of the marketplace card associated with this event.
     * @return JSON representation of the expiry event.
     */
    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("card", expiringCard.constructJSONObject());
        return jsonObject;
    }


}
