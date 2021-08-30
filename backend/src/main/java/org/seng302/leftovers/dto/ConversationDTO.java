package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.Conversation;


@Getter
@ToString
public class ConversationDTO {
    private Long id;
    private JSONObject card;
    private JSONObject buyer;

    public ConversationDTO(Conversation conversation) {
        this.id = conversation.getId();
        // TODO When MarketplaceCard DTO is done this needs updating
        this.card = conversation.getCard().constructJSONObject();
        // TODO When User DTO is done this needs updating
        this.buyer = conversation.getBuyer().constructPublicJson();
    }
}
