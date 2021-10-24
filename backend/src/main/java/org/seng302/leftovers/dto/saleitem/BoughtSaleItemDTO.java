package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.product.ProductResponseDTO;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.BoughtSaleItem;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A DTO representing a purchased sale item
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class BoughtSaleItemDTO {
    private Long id;
    private UserResponseDTO buyer;
    private ProductResponseDTO product;
    private Integer interestCount;
    private BigDecimal price;
    private Integer quantity;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant saleDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant listingDate;


    /**
     * Converts a BoughtSaleItem to its JSON form, with the buyer
     * @param item BroughtSaleItem to serialize
     */
    public BoughtSaleItemDTO(BoughtSaleItem item, boolean withBuyer) {
        this.id = item.getId();
        if (withBuyer) {
            this.buyer = new UserResponseDTO(item.getBuyer());
        }
        this.product = new ProductResponseDTO(item.getProduct());
        this.interestCount = item.getInterestCount();
        this.price = item.getPrice();
        this.quantity = item.getQuantity();
        this.saleDate = item.getSaleDate();
        this.listingDate = item.getListingDate();
    }

    /**
     * Constructor for helping with building this object from a string
     */
    protected BoughtSaleItemDTO(){}
}
