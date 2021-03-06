package org.seng302.leftovers.dto.conversation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.card.MarketplaceCardResponseDTO;
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
    private MarketplaceCardResponseDTO card;
    private UserResponseDTO buyer;

    /**
     * Converts a Conversation entity to its JSON form
     * @param conversation Conversation to serialise
     */
    public ConversationDTO(Conversation conversation) {
        this.id = conversation.getId();
        this.card = new MarketplaceCardResponseDTO(conversation.getCard());
        this.buyer = new UserResponseDTO(conversation.getBuyer());
    }
}
