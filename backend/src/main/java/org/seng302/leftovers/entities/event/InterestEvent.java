package org.seng302.leftovers.entities.event;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.InterestEventDTO;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.entities.User;

import javax.persistence.*;

/**
 * Event for a message sent by an administrator to a user
 */
@Entity
public class InterestEvent extends Event {

    @ManyToOne
    @JoinColumn(name = "sale_item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SaleItem saleItem;

    @Column(nullable = false)
    private boolean interested;

    /**
     * Creates a new interest event for the provided user and sale item combination
     * Note that the created event will be in the "liked" interest state
     * @param notifiedUser User to notify
     * @param saleItem Sale item the user has liked
     */
    public InterestEvent(User notifiedUser, SaleItem saleItem) {
        super(notifiedUser);
        this.saleItem = saleItem;
        this.interested = true;
    }

    /**
     * Constructor required for JPA
     */
    protected InterestEvent() {}

    /**
     * Gets the sale item for this event
     * @return Event's sale item
     */
    public SaleItem getSaleItem() {
        return saleItem;
    }

    /**
     * Gets the interest state of this event (true=liked, false=unliked)
     * @return Interest state
     */
    public boolean getInterested() {
        return interested;
    }

    /**
     * Updates the interest state for this event
     * @param interested Updated interest state (true=liked, false=unliked)
     */
    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    /**
     * Constructs a new DTO representing this event
     * @return This InterestEvent as a DTO
     */
    @Override
    public InterestEventDTO asDTO() {
        return new InterestEventDTO(this);
    }
}
