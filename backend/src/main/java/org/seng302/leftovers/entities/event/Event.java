package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.seng302.leftovers.entities.User;
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
    private EventTag eventTag = EventTag.NONE;

    @ManyToOne
    @JoinColumn(name = "event_user", nullable = false)
    private User notifiedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus eventStatus = EventStatus.NORMAL;

    @Column(nullable = false)
    private boolean isRead = false;

    protected Event() {} // Required by JPA

    /**
     * Construct new event
     * @param notifiedUser User that the event will notify
     */
    protected Event(User notifiedUser) {
        this.notifiedUser = notifiedUser;
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
    public EventTag getTag() {
        return eventTag;
    }

    /**
     * Changes the tag for the event
     * @param eventTag New event tag
     */
    public void setTag(EventTag eventTag) {
        if (eventTag == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag cannot be null");
        }
        this.eventTag = eventTag;
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

    /**
     * Converts this event into a DTO
     * @return DTO for JSON serialisation
     */
    public abstract EventDTO asDTO();

    /**
     * Updates the isRead status to true if the event has been read.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Gets the isRead property of this event entity.
     * For now, it is only used for testing.
     * @return isRead boolean property
     */
    public boolean isRead() { return this.isRead; }

    /**
     * Returns the eventStatus of this event
     * @return the eventStatus enum
     */
    public EventStatus getStatus() { return this.eventStatus; }

    /**
     * Update the status of the event. Uses an enum for the different types
     * @param eventStatus Possible enum types are ARCHIVED, NORMAL, STARRED
     */
    public void updateEventStatus(EventStatus eventStatus) {
        if (this.eventStatus.equals(EventStatus.ARCHIVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The status of an archived event cannot be changed");
        }
        this.eventStatus = eventStatus;
    }
}
