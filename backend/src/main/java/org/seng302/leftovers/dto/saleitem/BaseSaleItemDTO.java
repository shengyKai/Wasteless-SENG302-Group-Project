package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.entities.SaleItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A SaleItemDTO for representing a SaleItem object
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class BaseSaleItemDTO {
    private long id;
    private InventoryItemResponseDTO inventoryItem;
    private int quantity;
    private BigDecimal price;
    private String moreInfo;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    private LocalDate closes;

    /**
     * Helper JSON constructor
     */
    protected BaseSaleItemDTO() {}

    /**
     * The SaleItemDTO constructor
     * @param saleItem the SaleItem object
     */
    public BaseSaleItemDTO(SaleItem saleItem) {
        this.id = saleItem.getId();
        this.inventoryItem = new InventoryItemResponseDTO(saleItem.getInventoryItem());
        this.quantity = saleItem.getQuantity();
        this.price = saleItem.getPrice();
        this.moreInfo = saleItem.getMoreInfo();
        this.created = saleItem.getCreated();
        this.closes = saleItem.getCloses();
    }
}
