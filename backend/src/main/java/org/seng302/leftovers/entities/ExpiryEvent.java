package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ExpiryEvent extends Event{

    @OneToOne
    private MarketplaceCard expiringCard;

    protected ExpiryEvent() {

    }

    public ExpiryEvent(MarketplaceCard expiringCard) {
        this.expiringCard = expiringCard;
    }

    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("card", expiringCard.constructJSONObject());
        return jsonObject;
    }


}
