package org.seng302.leftovers.dto.saleitem;
import org.seng302.leftovers.dto.business.BusinessType;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO with all the parameters to pass to GET business/listings/search
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SaleListingSearchExternalDTO {
    private String basicSearchQuery;
    private String productSearchQuery;
    private String businessSearchQuery;
    private String locationSearchQuery;
    private Integer page;
    private Integer resultsPerPage;
    private Boolean reverse;
    private String orderBy;
    private List<BusinessType> businessTypes;
    private BigDecimal priceLower;
    private BigDecimal priceUpper;
    private String closeLower;
    private String closeUpper;
}
