package org.seng302.leftovers.dto.business;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum representing the valid types for a business
 * Also functions as a DTO
 */
public enum BusinessType {
    @JsonProperty("Accommodation and Food Services")
    ACCOMMODATION_AND_FOOD_SERVICES,

    @JsonProperty("Retail Trade")
    RETAIL_TRADE,

    @JsonProperty("Charitable organisation")
    CHARITABLE,

    @JsonProperty("Non-profit organisation")
    NON_PROFIT,
}
