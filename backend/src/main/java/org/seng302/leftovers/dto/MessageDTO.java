package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Message;

import java.time.Instant;

/**
 * A DTO representing a Message
 * TODO Potentially merge this with SendMessageDTO?
 */
@Getter
@ToString
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Instant created;
    private String content;

    public MessageDTO(Message message) {
        this.id = message.getId();
        this.senderId = message.getSender().getUserID();
        this.created = message.getCreated();
        this.content = message.getContent();
    }
}
