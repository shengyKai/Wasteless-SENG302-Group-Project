package org.seng302.leftovers.dto.card;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A DTO for modifying a marketplace card
 */
@Getter
@ToString
@EqualsAndHashCode
public class ModifyMarketplaceCardDTO {

    @NotNull
    private MarketplaceCard.Section section = null;
    @NotNull
    private String title = null;
    private String description;
    @NotNull
    private List<Long> keywordIds;

}

