package org.seng302.leftovers.service;


import org.junit.jupiter.api.Test;
import org.seng302.leftovers.dto.saleitem.ReportGranularity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;


    @Test
    void foo() {
        var monday = LocalDate.parse("2021-09-27");
        var tuesday = LocalDate.parse("2022-09-28");

        var ranges = reportService.getRanges(monday, tuesday, ReportGranularity.MONTHLY);

        System.out.println(ranges);
    }
}
