package org.seng302.leftovers.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum representing the role of a user
 * Also functions as a DTO
 */
public enum UserRole {
    @JsonProperty("user")
    USER,
    @JsonProperty("globalApplicationAdmin")
    GAA,
    @JsonProperty("defaultGlobalApplicationAdmin")
    DGAA
}
