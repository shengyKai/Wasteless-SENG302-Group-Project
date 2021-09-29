package org.seng302.leftovers.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemRecord;
import org.seng302.leftovers.dto.saleitem.ReportGranularity;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.BoughtSaleItemRepository;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReportServiceTest {

    private ReportService reportService;

    @Mock
    private ReportGranularity granularity;
    @Mock
    private BoughtSaleItemRepository boughtSaleItemRepository;
    @Mock
    private Business business;
    @Mock
    private Specification<BoughtSaleItem> businessSpec;
    @Mock
    private Specification<BoughtSaleItem> periodSpec;
    @Mock
    private Specification<BoughtSaleItem> combinedSpec;
    @Mock
    private ReportService.DateRange range1;
    @Mock
    private ReportService.DateRange range2;
    @Mock
    private BoughtSaleItem boughtSaleItem;

    private MockedStatic<SearchSpecConstructor> searchSpecConstructor;
    private MockedConstruction<BoughtSaleItemRecord> boughtSaleItemRecordConstruction;

    private final Instant referenceInstant = Instant.parse("2021-09-08T08:47:59Z");

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(granularity.adjustEnd(any())).thenAnswer(invocation -> ((LocalDate) invocation.getArgument(0)).with(TemporalAdjusters.lastDayOfMonth()));

        searchSpecConstructor = Mockito.mockStatic(SearchSpecConstructor.class);
        boughtSaleItemRecordConstruction = Mockito.mockConstruction(BoughtSaleItemRecord.class);

        searchSpecConstructor.when(() -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromBusiness(business)).thenReturn(businessSpec);
        searchSpecConstructor.when(() -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(any(), any())).thenReturn(periodSpec);

        when(businessSpec.and(periodSpec)).thenReturn(combinedSpec);
        when(periodSpec.and(businessSpec)).thenReturn(combinedSpec);

        when(boughtSaleItemRepository.findAll(combinedSpec)).thenReturn(List.of());

        reportService = mock(ReportService.class, withSettings().spiedInstance(new ReportService(boughtSaleItemRepository)).defaultAnswer(RETURNS_DEFAULTS));
    }

    @AfterEach
    void tearDown() {
        searchSpecConstructor.close();
        boughtSaleItemRecordConstruction.close();
    }


    @ParameterizedTest
    @CsvSource({
            "2021-09-10,2021-09-23", // Contains within
            "2021-09-01,2021-09-30" // Exact overlap
    })
    void getRanges_startAndEndWithinGranularity_startEndReturned(LocalDate start, LocalDate end) {
        when(reportService.getRanges(any(), any(), any())).thenCallRealMethod();

        var actual = reportService.getRanges(start, end, granularity);
        var expected = List.of(new ReportService.DateRange(start, end));

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2021-08-10,2021-09-20",
            "2021-08-01,2021-09-30"
    })
    void getRanges_startAndEndOverlapsGranule_twoRangesReturned(LocalDate start, LocalDate end) {
        when(reportService.getRanges(any(), any(), any())).thenCallRealMethod();
        var actual = reportService.getRanges(start, end, granularity);
        var expected = List.of(
                new ReportService.DateRange(start, LocalDate.parse("2021-08-31")),
                new ReportService.DateRange(LocalDate.parse("2021-09-01"), end)
        );

        assertEquals(expected, actual);
    }

    @Test
    void getRanges_startAndEndOverlapsManyGranules_manyRangesReturned() {
        when(reportService.getRanges(any(), any(), any())).thenCallRealMethod();
        LocalDate start = LocalDate.parse("2021-08-02");
        LocalDate end = LocalDate.parse("2021-11-17");

        var actual = reportService.getRanges(start, end, granularity);
        var expected = List.of(
                new ReportService.DateRange(start, LocalDate.parse("2021-08-31")),
                new ReportService.DateRange(LocalDate.parse("2021-09-01"), LocalDate.parse("2021-09-30")),
                new ReportService.DateRange(LocalDate.parse("2021-10-01"), LocalDate.parse("2021-10-31")),
                new ReportService.DateRange(LocalDate.parse("2021-11-01"), end)
        );

        assertEquals(expected, actual);
    }

    @Test
    void dateRangeStartInstant_withStart_expectedResult() {
        var start = LocalDate.parse("2021-08-03");
        var range = new ReportService.DateRange(start, null);

        var expected = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        assertEquals(expected, range.startInstant());
    }

    @Test
    void dateRangeEndInstant_withEnd_expectedResult() {
        var end = LocalDate.parse("2021-08-03");
        var range = new ReportService.DateRange(null, end);

        var expected = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        assertEquals(expected, range.endInstant());
    }

    @Test
    void dateRangeInstants_withStartAndEndSeparatedBy1Day_have2DaySeparation() {
        var start = LocalDate.parse("2021-08-02");
        var end = LocalDate.parse("2021-08-03");
        var range = new ReportService.DateRange(start, end);

        var expected = Duration.of(2, ChronoUnit.DAYS);
        var actual = Duration.between(range.startInstant(), range.endInstant());
        assertEquals(expected, actual);
    }

    @Test
    void dateRangeInstants_backToBackRanges_haveNoGapsBetween() {
        var date = LocalDate.parse("2021-08-02");
        var firstRange = new ReportService.DateRange(null, date);
        var secondRange = new ReportService.DateRange(date.plusDays(1), null);

        var actual = Duration.between(firstRange.endInstant(), secondRange.startInstant());
        assertEquals(Duration.ZERO, actual);
    }

    @Test
    void generateReport_singleRange_expectedFilteringOccurred() {
        when(reportService.generateReport(any(), any(), any(), any())).thenCallRealMethod();

        when(range1.startInstant()).thenReturn(referenceInstant.plus(0, ChronoUnit.DAYS));
        when(range1.endInstant()).thenReturn(referenceInstant.plus(1, ChronoUnit.DAYS));

        when(reportService.getRanges(any(), any(), any())).thenReturn(List.of(
                range1
        ));

        reportService.generateReport(business, LocalDate.parse("2021-08-02"), LocalDate.parse("2021-08-10"), granularity);

        verify(reportService, times(1)).getRanges(LocalDate.parse("2021-08-02"), LocalDate.parse("2021-08-10"), granularity);

        searchSpecConstructor.verify(times(1),
                () -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(referenceInstant.plus(0, ChronoUnit.DAYS), referenceInstant.plus(1, ChronoUnit.DAYS))
        );
        searchSpecConstructor.verify(times(1),
                () -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromBusiness(business)
        );
        searchSpecConstructor.verifyNoMoreInteractions();

        verify(boughtSaleItemRepository, times(1)).findAll(combinedSpec);
    }

    @Test
    void generateReport_multipleRanges_rangesConstructed() {
        when(reportService.generateReport(any(), any(), any(), any())).thenCallRealMethod();

        when(range1.startInstant()).thenReturn(referenceInstant.plus(0, ChronoUnit.DAYS));
        when(range1.endInstant()).thenReturn(referenceInstant.plus(1, ChronoUnit.DAYS));
        when(range2.startInstant()).thenReturn(referenceInstant.plus(1, ChronoUnit.DAYS));
        when(range2.endInstant()).thenReturn(referenceInstant.plus(2, ChronoUnit.DAYS));

        when(reportService.getRanges(any(), any(), any())).thenReturn(List.of(
                range1,
                range2
        ));

        when(boughtSaleItemRepository.findAll(combinedSpec)).thenReturn(List.of(boughtSaleItem));


        var results = reportService.generateReport(business, LocalDate.parse("2021-08-02"), LocalDate.parse("2021-08-10"), granularity);


        searchSpecConstructor.verify(times(1),
                () -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(referenceInstant.plus(0, ChronoUnit.DAYS), referenceInstant.plus(1, ChronoUnit.DAYS))
        );
        searchSpecConstructor.verify(times(1),
                () -> SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(referenceInstant.plus(1, ChronoUnit.DAYS), referenceInstant.plus(2, ChronoUnit.DAYS))
        );
        verify(boughtSaleItemRepository, times(2)).findAll(combinedSpec);

        List<BoughtSaleItemRecord> records = boughtSaleItemRecordConstruction.constructed();

        assertEquals(2, records.size());
        assertEquals(records, results);
    }
}
