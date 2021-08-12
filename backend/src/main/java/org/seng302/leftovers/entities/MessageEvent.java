package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "participant_type"}))
public class MessageEvent extends Event {

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "participant_type")
    private ParticipantType participantType;

    private enum ParticipantType {
        BUYER,
        SELLER
    }

    public MessageEvent(User notifiedUser, Message message) {
        super(notifiedUser);
        if (message.getConversation().getBuyer().equals(notifiedUser)) {
            this.participantType = ParticipantType.BUYER;
        } else if (message.getConversation().getCard().getCreator().equals(notifiedUser)) {
            this.participantType = ParticipantType.SELLER;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Notification can only be sent to buyer or seller of card");
        }
        this.message = message;
        this.conversation = message.getConversation();
    }

    protected MessageEvent() {

    }

    public Message getMessage() {
        return message;
    }

    public ParticipantType getParticipantType() {
        return participantType;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("message", message.constructJSONObject());
        jsonObject.appendField("card", conversation.getCard().constructJSONObject());
        return jsonObject;
    }
}
