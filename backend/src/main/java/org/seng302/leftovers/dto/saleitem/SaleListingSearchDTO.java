package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.business.BusinessType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A DTO representing a sale listing search specification
 */
@Getter
@ToString
@EqualsAndHashCode
public class SaleListingSearchDTO {
    private BigDecimal priceLowerBound;
    private BigDecimal priceUpperBound;
    private LocalDate closingDateLowerBound;
    private LocalDate closingDateUpperBound;
    private List<BusinessType> businessTypes;
    // TODO add the business name and other search requirements as stated in the ACs for story U29

    public SaleListingSearchDTO(BigDecimal priceLowerBound, BigDecimal priceUpperBound,
                                LocalDate closingDateLowerBound, LocalDate closingDateUpperBound, List<BusinessType> businessTypes) {
        this.priceLowerBound = priceLowerBound;
        this.priceUpperBound = priceUpperBound;
        this.closingDateLowerBound = closingDateLowerBound;
        this.closingDateUpperBound = closingDateUpperBound;
        this.businessTypes = businessTypes;
    }
}
