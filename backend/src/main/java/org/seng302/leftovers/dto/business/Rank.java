package org.seng302.leftovers.dto.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DTO representing the rank of a business
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Rank {
    BRONZE("bronze", 5),
    SILVER("silver", 20),
    GOLD("gold", 100),
    PLATINUM("platinum", null);

    private final String name;
    private final Integer threshold;

    /**
     * Gets the name of the rank
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of points required to reach the next rank
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Rank constructor
     * @param name Serialised name of rank
     * @param threshold Threshold value of the rank to the next rank
     */
    Rank(String name, Integer threshold) {
        this.name = name;
        this.threshold = threshold;
    }
}
