package org.seng302.leftovers.entities.event;

import org.seng302.leftovers.dto.event.GlobalMessageEventDTO;
import org.seng302.leftovers.dto.event.KeywordCreatedEventDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Event which is sent to system administrators to notify them of the creation of a new keyword.
 */
@Entity
public class KeywordCreatedEvent extends Event {

    @OneToOne
    @JoinColumn(name = "new_keyword", nullable = false)
    private Keyword newKeyword;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * Empty constructor to appease JPA
     */
    protected KeywordCreatedEvent() { }

    /**
     * Constructor for the create keyword event.
     * @param notifiedUser User to notify of the new keyword
     * @param creator User that made the new keyword
     * @param newKeyword The keyword which has been created.
     */
    public KeywordCreatedEvent(User notifiedUser, User creator, Keyword newKeyword) {
        super(notifiedUser);
        this.newKeyword = newKeyword;
        this.creator = creator;
    }

    /**
     * Returns the keyword which is associated with this event.
     * @return The keyword associated with this event.
     */
    public Keyword getNewKeyword() {
        return newKeyword;
    }

    /**
     * Returns the user who created the keyword associated with this event.
     * @return Creator of the keyword.
     */
    public User getCreator() {
        return creator;
    }

    @Override
    public KeywordCreatedEventDTO asDTO() {
        return new KeywordCreatedEventDTO(this);
    }
}
