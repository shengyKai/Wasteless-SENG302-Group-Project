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
@EqualsAndHashCode(callSuper = false)
public class MessageDTO extends SendMessageDTO {
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    private String content;

    /**
     * Converts a Message entity to its JSON form
     * @param message Message to serialise
     */
    public MessageDTO(Message message) {
        super(message);
        this.created = message.getCreated();
        this.content = message.getContent();
    }
}
