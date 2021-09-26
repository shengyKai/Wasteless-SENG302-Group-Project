package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.*;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * A DTO representing a sale listing search specification
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class SaleListingSearchDTO {
    private BigDecimal priceLowerBound;
    private BigDecimal priceUpperBound;
    private LocalDate closingDateLowerBound;
    private LocalDate closingDateUpperBound;
    private List<BusinessType> businessTypes;
    private String basicSearchQuery;
    private String productSearchQuery;
    private String businessSearchQuery;
    private String locationSearchQuery;

    public SaleListingSearchDTO(SaleListingSearchExternalDTO externalDTO) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (externalDTO.getCloseLower() != null && !externalDTO.getCloseLower().isEmpty()) {
                this.closingDateLowerBound = LocalDate.parse(externalDTO.getCloseLower(), formatter);
            }
            if (externalDTO.getCloseUpper() != null && !externalDTO.getCloseUpper().isEmpty()) {
                this.closingDateUpperBound = LocalDate.parse(externalDTO.getCloseUpper(), formatter);
            }
        } catch (DateTimeParseException badDate) {
            throw new ValidationResponseException("Close date parameters were not in date format");
        }
        this.priceLowerBound = externalDTO.getPriceLower();
        this.priceUpperBound = externalDTO.getPriceUpper();
        try {
            this.businessTypes = objectMapper.convertValue(externalDTO.getBusinessTypes(), new TypeReference<>() {
            });
        } catch (IllegalArgumentException e) {
            throw new ValidationResponseException("Invalid business types");
        }
        this.basicSearchQuery = externalDTO.getBasicSearchQuery();
        this.productSearchQuery = externalDTO.getProductSearchQuery();
        this.businessSearchQuery = externalDTO.getBusinessSearchQuery();
        this.locationSearchQuery = externalDTO.getLocationSearchQuery();
    }
}
