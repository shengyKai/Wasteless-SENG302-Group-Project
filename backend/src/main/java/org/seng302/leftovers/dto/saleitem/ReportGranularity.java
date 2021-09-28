package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

/**
 * Enum representing valid granularities for a sale listing report
 */
public enum ReportGranularity {
    @JsonProperty("daily")
    DAILY(null),
    @JsonProperty("weekly")
    WEEKLY(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
    @JsonProperty("monthly")
    MONTHLY(TemporalAdjusters.lastDayOfMonth()),
    @JsonProperty("yearly")
    YEARLY(TemporalAdjusters.lastDayOfYear()),
    @JsonProperty("none")
    NONE(new MaximumAdjuster());

    private final TemporalAdjuster endPeriodAdjuster;

    ReportGranularity(TemporalAdjuster endPeriodAdjuster) {
        this.endPeriodAdjuster = endPeriodAdjuster;
    }

    /**
     * Adjusts the provided date into the end of the current period for this granularity
     * @param date Date to shift back
     * @return Shifted back date
     */
    public LocalDate adjustEnd(LocalDate date) {
        if (endPeriodAdjuster == null) {
            return date;
        }
        return date.with(endPeriodAdjuster);
    }

    /**
     * Temporal adjuster that shifts all inputs to their maximum values
     */
    private static class MaximumAdjuster implements TemporalAdjuster {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            if (temporal instanceof LocalDate) {
                return LocalDate.MAX;
            }
            throw new UnsupportedOperationException("Only LocalDates are supported");
        }
    }
}
