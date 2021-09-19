package org.seng302.leftovers.dto.saleitem;

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
    private Instant saleDate;
    private Instant listingDate;

    public BoughtSaleItemDTO(BoughtSaleItem item) {
        this.id = item.getId();
        this.buyer = new UserResponseDTO(item.getBuyer());
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
