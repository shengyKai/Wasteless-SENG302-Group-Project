package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ToString
@Getter
public class ModifyBusinessDTO extends CreateBusinessDTO {
    @NotNull
    private Boolean updateProductCountry;
}
