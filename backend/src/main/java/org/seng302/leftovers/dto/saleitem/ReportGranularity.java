package org.seng302.leftovers.dto.saleitem;

import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@ToString
public enum ReportGranularity {
    // TODO @JsonValue
    DAILY(null),
    WEEKLY(TemporalAdjusters.next(DayOfWeek.MONDAY)),
    MONTHLY(TemporalAdjusters.lastDayOfMonth()),
    YEARLY(TemporalAdjusters.lastDayOfYear()),
    NONE(new MaximumAdjuster());

    private final TemporalAdjuster endPeriodAdjuster;

    ReportGranularity(TemporalAdjuster endPeriodAdjuster) {
        this.endPeriodAdjuster = endPeriodAdjuster;
    }

    /**
     * TODO
     * @param date
     * @return
     */
    public LocalDate adjustEnd(LocalDate date) {
        if (endPeriodAdjuster == null) {
            return date;
        }
        return date.with(endPeriodAdjuster);
    }

    /**
     * TODO
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
