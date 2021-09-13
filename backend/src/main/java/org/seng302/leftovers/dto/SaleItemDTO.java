package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.tools.JsonTools;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * TODO
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

    /**
     * Helper JSON constructor
     */
    protected SaleItemDTO() {}

    /**
     * TODO
     * @param saleItem
     */
    public SaleItemDTO(SaleItem saleItem) {
        this.id = saleItem.getId();
        this.inventoryItem = new InventoryItemDTO(saleItem.getInventoryItem());
        this.quantity = saleItem.getQuantity();
        this.price = saleItem.getPrice();
        this.moreInfo = saleItem.getMoreInfo();
        this.created = saleItem.getCreated();
        this.closes = saleItem.getCloses();
    }
}
