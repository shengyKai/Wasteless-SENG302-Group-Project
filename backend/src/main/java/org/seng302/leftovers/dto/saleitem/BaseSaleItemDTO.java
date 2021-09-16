package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.SaleItem;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A SaleItemDTO for representing a SaleItem object
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class BaseSaleItemDTO {
    @NotNull
    private Integer quantity;
    private BigDecimal price;
    private String moreInfo;
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
        this.quantity = saleItem.getQuantity();
        this.price = saleItem.getPrice();
        this.moreInfo = saleItem.getMoreInfo();
        this.closes = saleItem.getCloses();
    }
}
