package org.seng302.leftovers.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import net.minidev.json.JSONObject;

/**
 * Event which is sent to system administrators to notify them of the creation of a new keyword.
 */
@Entity
public class CreateKeywordEvent extends Event {

    @OneToOne
    @JoinColumn(name = "new_keyword", unique = true, nullable = false)
    private Keyword newKeyword;

    /**
     * Empty constructor to appease JPA
     */
    protected CreateKeywordEvent() { }

    /**
     * Constructor for the create keyword event.
     * @param newKeyword The keyword which has been created.
     */
    public CreateKeywordEvent(Keyword newKeyword) {
        this.newKeyword = newKeyword;
    }

    /**
     * Returns the keyword which is associated with this event.
     * @return The keyword associated with this event.
     */
    public Keyword getNewKeyword() {
        return newKeyword;
    }

    /**
     * Construct a JSON representation of the create keyword event. Contains the event's id, created date and type, and
     * the JSON representation of the keyword associated with this event.
     * @return JSON representation of the create keyword event.
     */
    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("keyword", newKeyword.constructJSONObject());
        return jsonObject;
    }

}
