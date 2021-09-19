package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing a keyword entity
 */
@Getter
@ToString
@EqualsAndHashCode
public class CreateKeywordDTO {
    @NotNull
    private String name;
}
