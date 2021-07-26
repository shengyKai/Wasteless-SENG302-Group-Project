package org.seng302.leftovers.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import net.minidev.json.JSONObject;

@Entity
public class CreateKeywordEvent extends Event {

    @OneToOne
    @JoinColumn(name = "new_keyword", unique = true, nullable = false)
    private Keyword newKeyword;

    protected CreateKeywordEvent() {

    }

    public CreateKeywordEvent(Keyword newKeyword) {
        this.newKeyword = newKeyword;
    }

    /**
     * Returns the marketplace card nearing expiry which is associated with this event.
     * @return The marketplace card associated with this event.
     */
    public Keyword getNewKeyword() {
        return newKeyword;
    }

    /**
     * Construct a JSON representation of the expiry event. Contains the event's id, created date and type, and the JSON
     * representation of the marketplace card associated with this event.
     * @return JSON representation of the expiry event.
     */
    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("keyword", newKeyword.constructJSONObject());
        return jsonObject;
    }

    public Keyword getKeyword() {
        return newKeyword;
    }
}
