package org.seng302.leftovers.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.product.ProductResponseDTO;
import org.seng302.leftovers.entities.InventoryItem;


/**
 * A InventoryItemDTO for representing a InventoryItem being sent to a client
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class InventoryItemResponseDTO extends BaseInventoryItemDTO {
    private Long id;
    private ProductResponseDTO product;
    private int remainingQuantity;

    /**
     * Helper JSON constructor
     */
    protected InventoryItemResponseDTO() {}

    /**
     * Converts a InventoryItem entity to its JSON form
     * @param inventoryItem InventoryItem to serialise
     */
    public InventoryItemResponseDTO(InventoryItem inventoryItem) {
        super(inventoryItem);
        this.id = inventoryItem.getId();
        this.product = new ProductResponseDTO(inventoryItem.getProduct());
        this.remainingQuantity = inventoryItem.getRemainingQuantity();
    }
}
