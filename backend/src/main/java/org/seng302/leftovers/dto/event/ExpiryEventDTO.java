package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.event.ExpiryEvent;

/**
 * A DTO representing a ExpiryEvent
 */
@Getter
@ToString
public class ExpiryEventDTO extends EventDTO {
    private JSONObject card;

    public ExpiryEventDTO(ExpiryEvent event) {
        super(event);
        // TODO When MarketplaceCard DTO is done this needs updating
        this.card = event.getExpiringCard().constructJSONObject();
    }
}
