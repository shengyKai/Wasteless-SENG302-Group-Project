package org.seng302.leftovers.dto.business;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A DTO representing the parameters passed to a PUT /businesses/:id request
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = false)
public class ModifyBusinessDTO extends CreateBusinessDTO {
    @NotNull
    private Boolean updateProductCountry;
    private List<Long> imageIds = List.of();
}