package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;

/**
 * A DTO representing a KeywordCreatedEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class KeywordCreatedEventDTO extends EventDTO {
    private JSONObject keyword;
    private UserResponseDTO creator;

    /**
     * Converts a KeywordCreatedEvent entity to its JSON form
     * @param event KeywordCreatedEvent to serialise
     */
    public KeywordCreatedEventDTO(KeywordCreatedEvent event) {
        super(event);
        // TODO When Keyword DTO is done this needs updating
        this.keyword = event.getNewKeyword().constructJSONObject();
        this.creator = new UserResponseDTO(event.getCreator());
    }
}
