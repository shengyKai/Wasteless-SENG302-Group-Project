package org.seng302.leftovers.dto.event;

import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.event.DeleteEvent;
import org.seng302.leftovers.entities.event.ExpiryEvent;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;

/**
 * A DTO representing a KeywordCreatedEvent
 */
@Getter
@ToString
public class KeywordCreatedEventDTO extends EventDTO {
    private JSONObject keyword;
    private JSONObject creator;

    public KeywordCreatedEventDTO(KeywordCreatedEvent event) {
        super(event);
        // TODO When Keyword DTO is done this needs updating
        this.keyword = event.getNewKeyword().constructJSONObject();
        // TODO When User DTO is done this needs updating
        this.creator = event.getCreator().constructPublicJson();
    }
}
