package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.entities.SaleItem;

import java.time.Instant;

/**
 * TODO
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class SaleItemResponseDTO extends BaseSaleItemDTO {
    private Long id;
    private InventoryItemResponseDTO inventoryItem;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;

    /**
     * Helper JSON constructor
     */
    protected SaleItemResponseDTO() {}

    /**
     * The SaleItemDTO constructor
     * @param saleItem the SaleItem object
     */
    public SaleItemResponseDTO(SaleItem saleItem) {
        super(saleItem);
        this.id = saleItem.getId();
        this.inventoryItem = new InventoryItemResponseDTO(saleItem.getInventoryItem());
        this.created = saleItem.getCreated();
    }
}
