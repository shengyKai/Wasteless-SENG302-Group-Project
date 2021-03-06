package org.seng302.leftovers.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the tag assigned to an Event entity
 */
public enum EventTag {
    @JsonProperty("none")
    NONE,

    @JsonProperty("red")
    RED,

    @JsonProperty("orange")
    ORANGE,

    @JsonProperty("yellow")
    YELLOW,

    @JsonProperty("green")
    GREEN,

    @JsonProperty("blue")
    BLUE,

    @JsonProperty("purple")
    PURPLE
}
