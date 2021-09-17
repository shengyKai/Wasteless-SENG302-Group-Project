package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO for creating a marketplace card
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class CreateMarketplaceCardDTO extends ModifyMarketplaceCardDTO{
    @NotNull
    private Long creatorId = null;
}
