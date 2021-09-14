package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class MarketplaceCardDTO {
    private Long id;
    private User creator;
    private String section;
    private Instant created;
    private Instant lastRenewed;
    private Instant displayPeriodEnd;
    private String title;
    private String description;
    private List<KeywordDTO> keywords;

    public MarketplaceCardDTO(MarketplaceCard card) {
        this.id = card.getID();
        this.creator = card.getCreator();
        this.section = card.getSection().getName();
        this.created = card.getCreated();
        this.lastRenewed = card.getLastRenewed();
        this.displayPeriodEnd = card.getCloses();
        this.title = card.getTitle();
        this.description = card.getDescription();
        this.keywords = card.getKeywords().stream().map(KeywordDTO::new).collect(Collectors.toList());
    }
}
