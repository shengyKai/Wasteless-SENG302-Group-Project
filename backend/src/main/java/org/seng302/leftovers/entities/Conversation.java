package org.seng302.leftovers.entities;


import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the communication between a marketplace card creator and a prospective buyer
 *
 * In future this entity could be extended to support more than two participants and be associated with different topics
 */
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"card_id", "buyer_id"})
})
@Entity
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private MarketplaceCard card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    private List<Message> messages = new ArrayList<>();

    protected Conversation() {} // Required by JPA

    /**
     * Construct a new conversation
     * @param card Card that is under discussion
     * @param buyer Prospective buyer user
     */
    public Conversation(MarketplaceCard card, User buyer) {
        if (card.getCreator().equals(buyer)) {
            throw new InsufficientPermissionResponseException("You cannot create a conversation with yourself");
        }
        this.card = card;
        this.buyer = buyer;
    }

    /**
     * Gets the generated id
     * @return Conversation Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the card under discussion
     * @return Marketplace card which the conversation is about
     */
    public MarketplaceCard getCard() {
        return card;
    }

    /**
     * Gets the prospective buyer
     * @return User that may buy the card
     */
    public User getBuyer() {
        return buyer;
    }

    /**
     * Gets the list of messages sorted by creation time from most to least recent
     * @return List of messages that have been sent
     */
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", cardId=" + card.getID() +
                ", buyerId=" + buyer.getUserID() +
                '}';
    }
}
