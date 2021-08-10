package org.seng302.leftovers.dto;

import org.seng302.leftovers.entities.Tag;

import javax.validation.constraints.NotNull;

public class SetTagDTO {
    @NotNull
    public Tag value;
}
