package org.seng302.leftovers.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserRole {
    @JsonProperty("user")
    USER,
    @JsonProperty("globalApplicationAdmin")
    GAA,
    @JsonProperty("defaultGlobalApplicationAdmin")
    DGAA
}
