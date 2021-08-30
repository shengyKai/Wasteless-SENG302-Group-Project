package org.seng302.leftovers.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.ConversationDTO;
import org.seng302.leftovers.dto.MessageDTO;
import org.seng302.leftovers.entities.event.MessageEvent;

/**
 * A DTO representing a MessageEvent
 */
@Getter
@ToString
@EqualsAndHashCode
public class MessageEventDTO extends EventDTO {
    private MessageDTO message;
    private ConversationDTO conversation;
    private MessageEvent.ParticipantType participantType;


    /**
     * Converts a MessageEvent entity to its JSON form
     * @param event MessageEvent to serialise
     */
    public MessageEventDTO(MessageEvent event) {
        super(event);
        this.message = new MessageDTO(event.getMessage());
        this.conversation = new ConversationDTO(event.getConversation());
        this.participantType = event.getParticipantType();
    }
}
