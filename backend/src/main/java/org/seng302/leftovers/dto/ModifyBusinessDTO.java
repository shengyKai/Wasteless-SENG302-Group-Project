package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing the parameters passed to a PUT /businesses/:id request
 */
@ToString
@Getter
@EqualsAndHashCode
public class ModifyBusinessDTO extends CreateBusinessDTO {
    @NotNull
    private Boolean updateProductCountry;
}