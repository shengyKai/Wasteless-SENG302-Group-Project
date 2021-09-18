package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
public class SaleItemDTO {
    private long id;
    private InventoryItemDTO inventoryItem;
    private int quantity;
    private BigDecimal price;
    private String moreInfo;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    private LocalDate closes;
    private int interestCount;

    /**
     * Helper JSON constructor
     */
    protected SaleItemDTO() {}

    /**
     * The SaleItemDTO constructor
     * @param saleItem the SaleItem object
     */
    public SaleItemDTO(SaleItem saleItem) {
        this.id = saleItem.getId();
        this.inventoryItem = new InventoryItemDTO(saleItem.getInventoryItem());
        this.quantity = saleItem.getQuantity();
        this.price = saleItem.getPrice();
        this.moreInfo = saleItem.getMoreInfo();
        this.created = saleItem.getCreated();
        this.closes = saleItem.getCloses();
        this.interestCount = saleItem.getLikeCount();
    }
}
