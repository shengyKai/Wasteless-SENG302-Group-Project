package org.seng302.leftovers.dto.saleitem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * DTO for the request body of the PUT /listings/:id/interest
 */
@Getter
@ToString
@EqualsAndHashCode
public class SetSaleItemInterestDTO {
    @NotNull
    private Long userId;

    @NotNull
    private Boolean interested;
}
