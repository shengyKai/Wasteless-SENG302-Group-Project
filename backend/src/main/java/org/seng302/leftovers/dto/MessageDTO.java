package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Message;

import java.time.Instant;

/**
 * A DTO representing a Message entity
 */
@Getter
@ToString
@EqualsAndHashCode
public class MessageDTO {
    private Long id;
    private Long senderId;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    private String content;


    /**
     * Converts a Message entity to its JSON form
     * @param message Message to serialise
     */
    public MessageDTO(Message message) {
        this.id = message.getId();
        this.senderId = message.getSender().getUserID();
        this.created = message.getCreated();
        this.content = message.getContent();
    }
}
