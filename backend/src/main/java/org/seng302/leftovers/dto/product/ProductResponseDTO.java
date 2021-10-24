package org.seng302.leftovers.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.ImageDTO;
import org.seng302.leftovers.dto.business.BusinessResponseDTO;
import org.seng302.leftovers.entities.Product;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A DTO representing a product being sent to a client
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class ProductResponseDTO extends UpdateProductDTO {

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;
    private BusinessResponseDTO business;
    private List<ImageDTO> images;
    private String countryOfSale;

    /**
     * Helper JSON constructor
     */
    protected ProductResponseDTO() {}

    /**
     * Converts a Product entity to its JSON form
     * @param product Product to serialise
     */
    public ProductResponseDTO(Product product) {
        super(product);
        this.created = product.getCreated();
        this.business = BusinessResponseDTO.withoutAdmins(product.getBusiness());
        this.images = product.getImages().stream().map(ImageDTO::new).collect(Collectors.toList());
        this.countryOfSale = product.getCountryOfSale();
    }
}
