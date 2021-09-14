package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;

import javax.validation.constraints.NotNull;

/**
 * A DTO for modifying a marketplace card
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ModifyMarketplaceCardDTO {

    @NotNull
    private MarketplaceCard.Section section;
    @NotNull
    private String title;
    private String description;
    @NotNull
    private long[] keywordIds;
}

