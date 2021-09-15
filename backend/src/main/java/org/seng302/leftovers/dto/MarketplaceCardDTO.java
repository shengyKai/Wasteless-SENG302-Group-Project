package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
public class MarketplaceCardDTO {
    private Long id;
    private UserResponseDTO creator;
    private MarketplaceCard.Section section;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant lastRenewed;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant displayPeriodEnd;
    private String title;
    private String description;
    private List<KeywordDTO> keywords;

    public MarketplaceCardDTO(MarketplaceCard card) {
        this.id = card.getID();
        this.creator = new UserResponseDTO(card.getCreator());
        this.section = card.getSection();
        this.created = card.getCreated();
        this.lastRenewed = card.getLastRenewed();
        this.displayPeriodEnd = card.getCloses();
        this.title = card.getTitle();
        this.description = card.getDescription();
        this.keywords = card.getKeywords().stream().map(KeywordDTO::new).collect(Collectors.toList());
    }
}
