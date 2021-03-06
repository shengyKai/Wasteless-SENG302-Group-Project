package org.seng302.leftovers.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Product;

import java.math.BigDecimal;


/**
 * A DTO representing a request to create/update a product
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class UpdateProductDTO {

    private String id;
    private String name;
    private String description;
    private String manufacturer;
    private BigDecimal recommendedRetailPrice;

    /**
     * Helper JSON constructor
     */
    protected UpdateProductDTO() {}

    /**
     * Constructs a create/update product request from a product
     * This is only expected to be used by the ProductResponseDTO constructor
     * @param product Product to make request from
     */
    public UpdateProductDTO(Product product) {
        this.id = product.getProductCode();
        this.name = product.getName();
        this.description = product.getDescription();
        this.manufacturer = product.getManufacturer();
        this.recommendedRetailPrice = product.getRecommendedRetailPrice();
    }
}
