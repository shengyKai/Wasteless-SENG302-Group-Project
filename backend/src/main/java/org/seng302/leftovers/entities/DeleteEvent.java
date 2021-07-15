package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;

import javax.persistence.*;

/**
 * This class is used for notifying users when their marketplace card has been deleted.
 */
@Entity
public class DeleteEvent extends Event {

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private MarketplaceCard.Section section;

    @Column(nullable = false)
    private String title;

    protected DeleteEvent() {
    }

    public DeleteEvent(MarketplaceCard deletedCard) {
        section = deletedCard.getSection();
        title = deletedCard.getTitle();
    }

    /**
     * Returns the section of the deleted marketplace card
     * @return Deleted card section
     */
    public MarketplaceCard.Section getSection() {
        return section;
    }

    /**
     * Returns the title of the deleted marketplace card
     * @return Deleted card title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Construct a JSON representation of the delete event. Contains the event's id, created date and type, deleted card
     * title and card section.
     * @return JSON representation of the expiry event.
     */
    @Override
    public JSONObject constructJSONObject() {
        JSONObject jsonObject = super.constructJSONObject();
        jsonObject.appendField("title", title);
        jsonObject.appendField("section", section.getName());
        return jsonObject;
    }
}
