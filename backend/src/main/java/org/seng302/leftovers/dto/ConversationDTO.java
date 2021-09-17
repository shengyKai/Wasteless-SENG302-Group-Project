package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.Conversation;


/**
 * A DTO representing a Conversation entity
 */
@Getter
@ToString
@EqualsAndHashCode
public class ConversationDTO {
    private Long id;
    private JSONObject card;
    private UserResponseDTO buyer;

    /**
     * Converts a Conversation entity to its JSON form
     * @param conversation Conversation to serialise
     */
    public ConversationDTO(Conversation conversation) {
        this.id = conversation.getId();
        // TODO When MarketplaceCard DTO is done this needs updating
        this.card = conversation.getCard().constructJSONObject();
        this.buyer = new UserResponseDTO(conversation.getBuyer());
    }
}
