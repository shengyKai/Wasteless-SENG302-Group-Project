package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Event for a message sent by an administrator to a user
 */
@Entity
public class MessageEvent extends Event {
    @Column(nullable = false)
    private String message;

    protected MessageEvent() {}

    /**
     * Constructs a message event with the given initial message
     * @param message Initial event message
     */
    public MessageEvent(String message) {
        setMessage(message);
    }

    /**
     * Sets this event's message
     * @param message Message to set
     */
    public void setMessage(String message) {
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }
        this.message = message;
    }

    /**
     * Gets the message for this event
     * @return Event message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Constructs a JSON representation of this event
     * @return JSON object containing message event data
     */
    @Override
    public JSONObject constructJSONObject() {
        JSONObject json = super.constructJSONObject();
        json.put("message", message);
        return json;
    }
}
