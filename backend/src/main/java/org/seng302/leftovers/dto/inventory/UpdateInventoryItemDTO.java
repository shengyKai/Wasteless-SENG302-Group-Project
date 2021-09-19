package org.seng302.leftovers.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.InventoryItem;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * DTO representing a request to create/update a InventoryItem
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class UpdateInventoryItemDTO extends BaseInventoryItemDTO {
    @NotNull
    private String productId;

    /**
     * Helper JSON constructor
     */
    protected UpdateInventoryItemDTO() {}
}
