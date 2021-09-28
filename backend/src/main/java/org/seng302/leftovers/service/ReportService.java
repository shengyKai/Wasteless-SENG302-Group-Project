package org.seng302.leftovers.service;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemRecord;
import org.seng302.leftovers.dto.saleitem.ReportGranularity;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.persistence.BoughtSaleItemRepository;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
@Service
public class ReportService {


    private final BoughtSaleItemRepository boughtSaleItemRepository;


    public ReportService(BoughtSaleItemRepository boughtSaleItemRepository) {
        this.boughtSaleItemRepository = boughtSaleItemRepository;
    }

    /**
     * Object representing a start and end date
     */
    @ToString
    @AllArgsConstructor
    private static class DateRange {
        LocalDate start;
        LocalDate end;

        Instant startInstant() {
            return start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        Instant endInstant() {
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

        ranges.add(new DateRange(start, granularity.adjustEnd(start)));

        while (true) {
            var previousRange = ranges.get(ranges.size() - 1);
            if (!end.isAfter(previousRange.end)) {
                ranges.set(ranges.size() - 1, new DateRange(previousRange.start, end)); // Trim off excess
                break;
            }
            var nextStart = previousRange.end.plusDays(1);
            ranges.add(new DateRange(nextStart, granularity.adjustEnd(nextStart)));
        }
        return ranges;
    }


    /**
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
