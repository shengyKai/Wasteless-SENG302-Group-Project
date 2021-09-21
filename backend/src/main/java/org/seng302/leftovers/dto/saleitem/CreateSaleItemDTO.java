package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * DTO representing the request body of a POST /businesses/:id/listings request
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class CreateSaleItemDTO extends BaseSaleItemDTO {
    @NotNull
    private Long inventoryItemId;

    /**
     * Helper JSON constructor
     */
    protected CreateSaleItemDTO() {}
}
