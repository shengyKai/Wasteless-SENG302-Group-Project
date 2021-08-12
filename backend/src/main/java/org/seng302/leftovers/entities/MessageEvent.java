package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;

import javax.persistence.*;

@Entity
public class MessageEvent extends Event {

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    public MessageEvent(User notifiedUser, Message message) {
        super(notifiedUser);
        this.message = message;
    }

    protected MessageEvent() {

    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("message", message.constructJSONObject());
        jsonObject.appendField("card", message.getConversation().getCard().constructJSONObject());
        return jsonObject;
    }
}
