package org.seng302.leftovers.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.MarketplaceCard;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A DTO representing a marketplace card being sent to a client
 */
@Getter
@ToString
@EqualsAndHashCode
public class MarketplaceCardResponseDTO {
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

    /**
     * Constructs the JSON representation of a marketplace card
     * @param card Card to represent
     */
    public MarketplaceCardResponseDTO(MarketplaceCard card) {
        this.id = card.getID();
        this.creator = new UserResponseDTO(card.getCreator());
        this.section = card.getSection();
        this.created = card.getCreated();
        this.lastRenewed = card.getLastRenewed();
        this.displayPeriodEnd = card.getCloses();
        this.title = card.getTitle();
        this.description = Optional.ofNullable(card.getDescription()).orElse("");
        this.keywords = card.getKeywords().stream().map(KeywordDTO::new).collect(Collectors.toList());
    }
}
