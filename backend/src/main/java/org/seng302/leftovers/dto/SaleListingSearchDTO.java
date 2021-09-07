package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@ToString
@EqualsAndHashCode
public class SaleListingSearchDTO {
    private BigDecimal priceLowerBound;
    private BigDecimal priceUpperBound;
    private LocalDate closingDateLowerBound;
    private LocalDate closingDateUpperBound;
    private String businessType;
    // TODO add the business name and other search requirements as stated in the ACs for story U29

    public SaleListingSearchDTO(BigDecimal priceLowerBound, BigDecimal priceUpperBound,
                                LocalDate closingDateLowerBound, LocalDate closingDateUpperBound, String businessType) {
        this.priceLowerBound = priceLowerBound;
        this.priceUpperBound = priceUpperBound;
        this.closingDateLowerBound = closingDateLowerBound;
        this.closingDateUpperBound = closingDateUpperBound;
        this.businessType = businessType;
    }
}
