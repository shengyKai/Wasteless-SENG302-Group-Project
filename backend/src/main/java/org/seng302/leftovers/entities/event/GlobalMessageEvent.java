package org.seng302.leftovers.entities.event;

import lombok.EqualsAndHashCode;
import org.seng302.leftovers.dto.event.GlobalMessageEventDTO;
import org.seng302.leftovers.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Event for a message sent by an administrator to a user
 */
@Entity
@EqualsAndHashCode(callSuper = false)
public class GlobalMessageEvent extends Event {
    @Column(nullable = false, name="global_message")
    private String globalMessage;

    protected GlobalMessageEvent() {}

    /**
     * Constructs a message event with the given initial message
     * @param notifiedUser User to send the message to
     * @param globalMessage Initial event message
     */
    public GlobalMessageEvent(User notifiedUser, String globalMessage) {
        super(notifiedUser);
        setGlobalMessage(globalMessage);
    }

    /**
     * Sets this event's message
     * @param content Message to set
     */
    public void setGlobalMessage(String content) {
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }
        this.globalMessage = content;
    }

    /**
     * Gets the message for this event
     * @return Event message
     */
    public String getGlobalMessage() {
        return globalMessage;
    }

    /**
     * Converts this event into a DTO
     * @return DTO for JSON serialisation
     */
    @Override
    public GlobalMessageEventDTO asDTO() {
        return new GlobalMessageEventDTO(this);
    }
}
