package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.BoughtSaleItem;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoughtSaleItemRecord {
    private Integer uniqueListingsSold;
    private Integer uniqueBuyers;
    private Integer uniqueProducts;
    private Integer totalQuantitySold = 0;
    private BigDecimal totalPriceSold = BigDecimal.ZERO;
    private Integer totalInterest = 0;
    private Double averageDaysToSell;

    public BoughtSaleItemRecord(List<BoughtSaleItem> items) {
        Set<Long> buyerIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        uniqueListingsSold = items.size();

        long totalSecondsToSell = 0L;
        for (var item : items) {
            totalQuantitySold += item.getQuantity();
            totalInterest += item.getInterestCount();
            totalPriceSold = totalPriceSold.add(item.getPrice());
            buyerIds.add(item.getBuyer().getUserID());
            productIds.add(item.getProduct().getID());

            totalSecondsToSell += ChronoUnit.SECONDS.between(item.getListingDate(), item.getSaleDate());
        }
        if (uniqueListingsSold == 0) {
            averageDaysToSell = null;
        } else {
            averageDaysToSell = (double) totalSecondsToSell / (uniqueListingsSold * 60 * 60 * 24);
        }
        uniqueBuyers = buyerIds.size();
        uniqueProducts = productIds.size();
    }
}
