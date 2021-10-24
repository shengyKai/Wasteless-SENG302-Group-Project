package org.seng302.leftovers.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EventStatus {
    @JsonProperty("archived")
    ARCHIVED,

    @JsonProperty("normal")
    NORMAL,

    @JsonProperty("starred")
    STARRED,
}
