package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.seng302.leftovers.dto.event.MessageEventDTO;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;

/**
 * Event which is sent to participants in a conversation when a new message is added to that conversation.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "participant_type"}))
@EqualsAndHashCode(callSuper = false)
public class MessageEvent extends Event {

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "participant_type")
    private ParticipantType participantType;

    /**
     * Represents the two types of participant in a conversation. The seller is the creator of the marketplace card,
     * while the buyer is the user who has contacted the seller.
     */
    public enum ParticipantType {

        @JsonProperty("buyer")
        BUYER,
        @JsonProperty("seller")
        SELLER
    }

    /**
     * Construct an event for notifying users of messages in a conversation. The notified user must be one of the
     * participants in the conversation.
     * @param notifiedUser User who will receive the notification.
     * @param message The message to notify the users of.
     */
    public MessageEvent(User notifiedUser, Message message) {
        super(notifiedUser);
        if (message.getConversation().getBuyer().getUserID().equals(notifiedUser.getUserID())) {
            this.participantType = ParticipantType.BUYER;
        } else if (message.getConversation().getCard().getCreator().getUserID().equals(notifiedUser.getUserID())) {
            this.participantType = ParticipantType.SELLER;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Notification can only be sent to buyer " +
                    "or seller of card");
        }
        this.message = message;
        this.conversation = message.getConversation();
    }

    /**
     * No arguments constructor to appease JPA.
     */
    protected MessageEvent() {

    }

    /**
     * @return The message associated with this event.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return The type of the conversation participant who is notified by this event.
     */
    public ParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * @return The conversation associated with this event.
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * Update the message associated with this event. Also update the created time of this event to simulate replacing
     * it with a new event.
     * @param message A message which must be from the conversation associated with this event.
     */
    public void setMessage(Message message) {
        if (this.conversation.getId().equals(message.getConversation().getId())) {
            this.message = message;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The message associated with a message" +
                    " event can only be changed to a new message in the original conversation.");
        }
        setCreated(Instant.now());
    }

    /**
     * Converts this event into a DTO
     * @return DTO for JSON serialisation
     */
    @Override
    public MessageEventDTO asDTO() {
        return new MessageEventDTO(this);
    }
}
