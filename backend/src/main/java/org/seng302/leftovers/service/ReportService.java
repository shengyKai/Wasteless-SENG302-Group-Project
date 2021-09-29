package org.seng302.leftovers.service;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemRecord;
import org.seng302.leftovers.dto.saleitem.ReportGranularity;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.persistence.BoughtSaleItemRepository;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating reports
 */
@Service
public class ReportService {
    private final BoughtSaleItemRepository boughtSaleItemRepository;


    @Autowired
    public ReportService(BoughtSaleItemRepository boughtSaleItemRepository) {
        this.boughtSaleItemRepository = boughtSaleItemRepository;
    }

    /**
     * Object representing a start and end date
     */
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class DateRange {
        public final LocalDate start;
        public final LocalDate end;

        /**
         * Gets the instant at the start of the date range
         * @return Start of range moment
         */
        public Instant startInstant() {
            return start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        /**
         * Gets the instant at the end of the date range (exclusive)
         * @return End of range moment
         */
        public Instant endInstant() {
            return end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
    }


    /**
     * Generates a list of date ranges rounded to the provided granularity (start/end not rounded) from a provided start and end date
     * @param start Date for the first range to start
     * @param end Date for the last range to end
     * @param granularity Unit of time to snap the ranges to
     * @return List of DateRanges
     */
    public List<DateRange> getRanges(LocalDate start, LocalDate end, ReportGranularity granularity) {
        List<DateRange> ranges = new ArrayList<>();

        var currentRange = new DateRange(start, granularity.adjustEnd(start));
        while (end.isAfter(currentRange.end)) {
            ranges.add(currentRange);
            var rangeStart = currentRange.end.plusDays(1);
            currentRange = new DateRange(rangeStart, granularity.adjustEnd(rangeStart));
        }
        ranges.add(new DateRange(currentRange.start, end));
        return ranges;
    }


    /**
     * Generates a list of BoughtSaleItemRecords within a given date range
     * @param business The business to generate the report for
     * @param start Date of the start of the report
     * @param end Date of the end of the report
     * @param granularity Unit of time to snap the report entries to
     * @return List of BoughtSaleItemRecords
     */
    public List<BoughtSaleItemRecord> generateReport(Business business, LocalDate start, LocalDate end, ReportGranularity granularity) {
        var ranges = getRanges(start, end, granularity);

        List<BoughtSaleItemRecord> records = new ArrayList<>();

        for (DateRange range : ranges) {
            var specification = SearchSpecConstructor.constructBoughtSaleListingSpecificationFromBusiness(business)
                    .and(SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(range.startInstant(), range.endInstant()));

            var items = boughtSaleItemRepository.findAll(specification);

            records.add(new BoughtSaleItemRecord(items));
        }

        return records;
    }
}
