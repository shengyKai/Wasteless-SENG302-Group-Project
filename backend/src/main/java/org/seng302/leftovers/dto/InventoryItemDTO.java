package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.product.ProductResponseDTO;
import org.seng302.leftovers.entities.InventoryItem;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * A InventoryItemDTO for representing a InventoryItem object
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class InventoryItemDTO {
    private Long id;
    private ProductResponseDTO product;
    private int quantity;
    private BigDecimal pricePerItem;
    private BigDecimal totalPrice;
    private LocalDate manufactured;
    private LocalDate sellBy;
    private LocalDate bestBefore;
    private LocalDate expires;
    private int remainingQuantity;

    /**
     * Helper JSON constructor
     */
    protected InventoryItemDTO() {}

    /**
     * The InventoryItemDTO
     * @param invItem the InventoryItem object
     */
    public InventoryItemDTO(InventoryItem invItem) {
        this.id = invItem.getId();
        this.product = new ProductResponseDTO(invItem.getProduct());
        this.quantity = invItem.getQuantity();
        this.remainingQuantity = invItem.getRemainingQuantity();
        this.pricePerItem = invItem.getPricePerItem();
        this.totalPrice = invItem.getTotalPrice();
        this.manufactured = invItem.getManufactured();
        this.sellBy = invItem.getSellBy();
        this.bestBefore = invItem.getBestBefore();
        this.expires = invItem.getExpires();
    }
}
