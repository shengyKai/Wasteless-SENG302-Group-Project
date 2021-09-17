package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.KeywordDTO;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;

/**
 * A DTO representing a KeywordCreatedEvent
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class KeywordCreatedEventDTO extends EventDTO {
    private KeywordDTO keyword;
    private UserResponseDTO creator;

    /**
     * Converts a KeywordCreatedEvent entity to its JSON form
     * @param event KeywordCreatedEvent to serialise
     */
    public KeywordCreatedEventDTO(KeywordCreatedEvent event) {
        super(event);
        this.keyword = new KeywordDTO(event.getNewKeyword());
        this.creator = new UserResponseDTO(event.getCreator());
    }
}
