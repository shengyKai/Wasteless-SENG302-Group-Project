package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a column to filter Product entities by
 */
public enum ProductFilterOption {
    @JsonProperty("name")
    NAME,

    @JsonProperty("description")
    DESCRIPTION,

    @JsonProperty("manufacturer")
    MANUFACTURER,

    @JsonProperty("productCode")
    PRODUCT_CODE
}
