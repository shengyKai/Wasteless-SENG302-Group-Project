package org.seng302.leftovers.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;

/**
 * Abstract event entity for some component that will appear on a user's newsfeed
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant created = Instant.now();

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Tag tag = Tag.NONE;

    @ManyToOne
    @JoinColumn(name = "event_user", nullable = false)
    private User notifiedUser;

    protected Event() {} // Required by JPA

    /**
     * Construct new event
     * @param notifiedUser User that the event will notify
     */
    protected Event(User notifiedUser) {
        this.notifiedUser = notifiedUser;
    }

    /**
     * Constructs a JSON representation of this event.
     * The fields provided by event are:
     *   id      - Long id of the event
     *   created - String creation time
     *   tag     - Tag of the event
     *   type    - Type name of the event (equal to class name)
     * Subclasses are expected to override this method and add their own attributes.
     * @return JSON object containing event data
     */
    public JSONObject constructJSONObject() {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject json = new JSONObject();
        json.appendField("id", this.getId());
        json.appendField("created", this.getCreated().toString());
        json.appendField("type", this.getClass().getSimpleName());
        json.appendField("tag", objectMapper.convertValue(this.getTag(), String.class));

        return json;
    }

    /**
     * Gets the public key for this event
     * @return Event public key
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the moment when this event was created
     * @return Event creation moment
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * Gets the tag for this event
     * @return Tag colour of the event
     */
    public Tag getTag() {
        return tag;
    }

    /**
     * Changes the tag for the event
     * @param tag New event tag
     */
    public void setTag(Tag tag) {
        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag cannot be null");
        }
        this.tag = tag;
    }

    /**
     * Update the date this event was created. Used when one event represents multiple notifications which replace
     * each other.
     * @param created The date the replacement event was created.
     */
    protected void setCreated(Instant created) {
        this.created = created;
    }

    /**
     * Returns the user that this event will appear on their home feed
     * @return The notified user
     */
    public User getNotifiedUser() {
        return notifiedUser;
    }
}
