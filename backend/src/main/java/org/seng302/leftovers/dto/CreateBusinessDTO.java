package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import javax.validation.constraints.NotNull;

@ToString
@Getter
public class CreateBusinessDTO {
    @NotNull
    private Long primaryAdministratorId;
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private LocationDTO address;
    @NotNull
    private String businessType; // In future it'd be nice if this was an enum
}
