package org.seng302.leftovers.dto;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Keyword;

import java.time.Instant;

/**
 * A DTO representing a keyword entity
 */
@Getter
@ToString
@EqualsAndHashCode
public class KeywordDTO {
    private long id;
    private String name;
    private Instant created;

    /**
     * Converts a DTO from a given keyword
     * @param keyword Keyword to convert to DTO
     */
    public KeywordDTO(Keyword keyword) {
        this.id = keyword.getID();
        this.created = keyword.getCreated();
        this.name = keyword.getName();
    }

}
