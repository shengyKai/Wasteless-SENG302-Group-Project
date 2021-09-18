package org.seng302.leftovers.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.business.BusinessResponseDTO;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.tools.JsonTools;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private List<JSONObject> images;
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
        // TODO When ImageDTO is done, then update this
        this.images = product.getProductImages().stream().map(Image::constructJSONObject).collect(Collectors.toList());
        this.countryOfSale = product.getCountryOfSale();
    }
}
