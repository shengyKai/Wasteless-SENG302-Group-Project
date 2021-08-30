package org.seng302.leftovers.entities.event;

import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.event.DeleteEventDTO;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.entities.MarketplaceCard;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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

    protected DeleteEvent() {} // Required by JPA

    /**
     * Creates a new event for a deleted card
     * @param deletedCard Card that will be deleted
     */
    public DeleteEvent(MarketplaceCard deletedCard) {
        super(deletedCard.getCreator());
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

    @Override
    public DeleteEventDTO asDTO() {
        return new DeleteEventDTO(this);
    }
}
