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
 * A base DTO for inventory item related requests and responses
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class BaseInventoryItemDTO {
    @NotNull
    private Integer quantity;
    private BigDecimal pricePerItem;
    private BigDecimal totalPrice;
    private LocalDate manufactured;
    private LocalDate sellBy;
    private LocalDate bestBefore;
    private LocalDate expires;

    /**
     * Helper JSON constructor
     */
    protected BaseInventoryItemDTO() {}

    /**
     * Constructs a BaseInventoryItemDTO from an InventoryItem
     * This is only expected to be used by the InventoryItemResponseDTO constructor
     * @param inventoryItem InventoryItem to make dto from
     */
    public BaseInventoryItemDTO(InventoryItem inventoryItem) {
        this.quantity = inventoryItem.getQuantity();
        this.pricePerItem = inventoryItem.getPricePerItem();
        this.totalPrice = inventoryItem.getTotalPrice();
        this.manufactured = inventoryItem.getManufactured();
        this.sellBy = inventoryItem.getSellBy();
        this.bestBefore = inventoryItem.getBestBefore();
        this.expires = inventoryItem.getExpires();
    }
}
