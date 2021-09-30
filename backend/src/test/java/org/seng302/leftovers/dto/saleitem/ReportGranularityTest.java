package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReportGranularityTest {
    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @EnumSource(ReportGranularity.class)
    void serialise_expectedFormat(ReportGranularity granularity) {
        var serialised = objectMapper.convertValue(granularity, String.class);
        assertEquals(granularity.toString().toLowerCase(), serialised);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2021-01-01", "2021-03-10"})
    void adjustEnd_daily_returnsSameDate(LocalDate date) {
        assertEquals(date, ReportGranularity.DAILY.adjustEnd(date));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-09-27,2021-10-03",
            "2021-09-26,2021-09-26",
            "2021-05-12,2021-05-16"
    })
    void adjustEnd_weekly_returnsDateAtEndOfWeek(LocalDate input, LocalDate expectedOutput) {
        assertEquals(expectedOutput, ReportGranularity.WEEKLY.adjustEnd(input));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-01-01,2021-01-31",
            "2021-01-31,2021-01-31",
            "2021-05-10,2021-05-31"
    })
    void adjustEnd_monthly_returnsDateAtEndOfMonth(LocalDate input, LocalDate expectedOutput) {
        assertEquals(expectedOutput, ReportGranularity.MONTHLY.adjustEnd(input));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-01-01,2021-12-31",
            "2021-12-31,2021-12-31",
            "2022-05-10,2022-12-31"
    })
    void adjustEnd_yearly_returnsDateAtEndOfYear(LocalDate input, LocalDate expectedOutput) {
        assertEquals(expectedOutput, ReportGranularity.YEARLY.adjustEnd(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2021-01-01", "2021-03-10"})
    void adjustEnd_none_returnsMaximumLocalDate(LocalDate date) {
        assertEquals(LocalDate.MAX, ReportGranularity.NONE.adjustEnd(date));
    }
}
