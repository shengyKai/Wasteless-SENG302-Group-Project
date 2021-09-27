package org.seng302.leftovers.dto.business;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;

import java.util.Arrays;
import java.util.Objects;

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

    /**
     * Compute the rank which should be awarded for a given number of points.
     * @param points The number of points to translate to a rank.
     * @return The rank which should be awarded.
     */
    public static Rank getRankFromPoints(int points) {
        for (Rank rank : Rank.values()) {
            if (rank.getThreshold() == null || points < rank.getThreshold()) {
                return rank;
            }
        }
        throw new InternalErrorResponseException("Error in rank thresholds: maximum rank should not have a threshold.");
    }

    /**
     * Return the rank which is one above the given rank. If there is no rank above the given rank, null will be returned.
     * @param currentRank The method will find the rank above this rank.
     * @return The rank one above the given rank.
     */
    public static Rank getNextRank(Rank currentRank) {
        if (currentRank.ordinal() + 1 < Rank.values().length) {
            return Rank.values()[currentRank.ordinal() + 1];
        }
        return null;
    }

    /**
     * Helper method used by Jackson to convert json objects into a rank.
     * This is only expected to be used in tests, since the user should not need to provide the threshold value to send
     * a rank to the backend.
     * @param name Name of Rank
     * @param threshold Threshold value
     * @return Parsed rank
     */
    @JsonCreator
    public static Rank forValues(@JsonProperty("name") String name,
                                     @JsonProperty("threshold") Integer threshold) {
        for (Rank rank : Rank.values()) {
            if (rank.getName().equals(name) && Objects.equals(rank.getThreshold(), threshold)) {
                return rank;
            }
        }
        return null;
    }
}
