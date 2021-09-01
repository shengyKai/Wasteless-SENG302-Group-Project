package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;

/**
 * A DTO representing a KeywordCreatedEvent
 */
@Getter
@ToString
@EqualsAndHashCode
public class KeywordCreatedEventDTO extends EventDTO {
    private JSONObject keyword;
    private JSONObject creator;

    /**
     * Converts a KeywordCreatedEvent entity to its JSON form
     * @param event KeywordCreatedEvent to serialise
     */
    public KeywordCreatedEventDTO(KeywordCreatedEvent event) {
        super(event);
        // TODO When Keyword DTO is done this needs updating
        this.keyword = event.getNewKeyword().constructJSONObject();
        // TODO When User DTO is done this needs updating
        this.creator = event.getCreator().constructPublicJson();
    }
}
