package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.seng302.leftovers.entities.BoughtSaleItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of statistics about a list of BoughtSaleItems purchased over a period.
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoughtSaleItemRecord {
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer uniqueListingsSold;
    private Integer uniqueBuyers;
    private Integer uniqueProducts;
    private Integer totalQuantitySold = 0;
    private BigDecimal totalPriceSold = BigDecimal.ZERO;
    private Double averageLikeCount = 0.0;
    private Double averageDaysToSell;

    /**
     * Construct a new Record from a list of BoughtSaleItems
     * @param startDate Date this record begins (inclusive)
     * @param endDate Date this record ends (inclusive)
     * @param items BoughtSaleItems to generate a report record for
     */
    public BoughtSaleItemRecord(LocalDate startDate, LocalDate endDate, List<BoughtSaleItem> items) {
        this.startDate = startDate;
        this.endDate = endDate;

        Set<Long> buyerIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        uniqueListingsSold = items.size();

        long totalSecondsToSell = 0L;
        for (var item : items) {
            totalQuantitySold += item.getQuantity();
            averageLikeCount += item.getInterestCount();
            totalPriceSold = totalPriceSold.add(item.getPrice());
            buyerIds.add(item.getBuyer().getUserID());
            productIds.add(item.getProduct().getID());

            totalSecondsToSell += ChronoUnit.SECONDS.between(item.getListingDate(), item.getSaleDate());
        }
        if (uniqueListingsSold == 0) {
            averageDaysToSell = null;
            averageLikeCount = null;
        } else {
            averageDaysToSell = (double) totalSecondsToSell / (uniqueListingsSold * 60 * 60 * 24);
            averageLikeCount /= uniqueListingsSold;
        }
        uniqueBuyers = buyerIds.size();
        uniqueProducts = productIds.size();
    }
}
