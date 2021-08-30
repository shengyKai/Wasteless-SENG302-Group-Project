package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Message;

import javax.validation.constraints.NotNull;

/**
 * DTO for sending a message between marketplace users.
 */
@Getter
@ToString
@EqualsAndHashCode
public class SendMessageDTO {
    @NotNull
    private Long senderId;
    @NotNull
    private String message;

    /**
     * Constructor for the message dto from a Message entity
     * Only used by the implementation of MessageDTO
     * @param message Entity to construct DTO for
     */
    protected SendMessageDTO(Message message) {
        this.senderId = message.getSender().getUserID();
        this.message = message.getContent();
    }
}
