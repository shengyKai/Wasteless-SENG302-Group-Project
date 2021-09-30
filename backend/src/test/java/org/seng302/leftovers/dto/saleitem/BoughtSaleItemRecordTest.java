package org.seng302.leftovers.dto.saleitem;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemRecord;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class BoughtSaleItemRecordTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private User user1;
    @Mock
    private User user2;

    @Mock
    private Product product1;
    @Mock
    private Product product2;

    @Mock
    private BoughtSaleItem item1;
    @Mock
    private BoughtSaleItem item2;
    @Mock
    private BoughtSaleItem item3;

    private final LocalDate startDate = LocalDate.parse("2021-01-10");
    private final LocalDate endDate = LocalDate.parse("2021-10-01");

    private final Instant referenceInstant = Instant.parse("2021-09-08T08:47:59Z");

    @BeforeEach
    void setup() {
        when(user1.getUserID()).thenReturn(1L);
        when(user2.getUserID()).thenReturn(2L);

        when(product1.getID()).thenReturn(1L);
        when(product2.getID()).thenReturn(2L);

        when(item1.getBuyer()).thenReturn(user2);
        when(item2.getBuyer()).thenReturn(user1);
        when(item3.getBuyer()).thenReturn(user2);

        when(item1.getQuantity()).thenReturn(3);
        when(item2.getQuantity()).thenReturn(5);
        when(item3.getQuantity()).thenReturn(7);

        when(item1.getInterestCount()).thenReturn(0);
        when(item2.getInterestCount()).thenReturn(3);
        when(item3.getInterestCount()).thenReturn(9);

        when(item1.getProduct()).thenReturn(product1);
        when(item2.getProduct()).thenReturn(product2);
        when(item3.getProduct()).thenReturn(product2);

        when(item1.getPrice()).thenReturn(new BigDecimal("7.0"));
        when(item2.getPrice()).thenReturn(new BigDecimal("6.0"));
        when(item3.getPrice()).thenReturn(new BigDecimal("3.0"));

        when(item1.getListingDate()).thenReturn(referenceInstant.plus(5, ChronoUnit.DAYS));
        when(item2.getListingDate()).thenReturn(referenceInstant.plus(9, ChronoUnit.DAYS));
        when(item3.getListingDate()).thenReturn(referenceInstant.plus(0, ChronoUnit.DAYS));

        when(item1.getSaleDate()).thenReturn(referenceInstant.plus(6, ChronoUnit.DAYS));
        when(item2.getSaleDate()).thenReturn(referenceInstant.plus(20, ChronoUnit.DAYS));
        when(item3.getSaleDate()).thenReturn(referenceInstant.plus(10, ChronoUnit.DAYS));
    }

    @Test
    void serialise_expectedFormat() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2));
        var json = objectMapper.convertValue(record, JSONObject.class);

        assertEquals(record.getStartDate().toString(), json.get("startDate"));
        assertEquals(record.getEndDate().toString(), json.get("endDate"));
        assertEquals(record.getUniqueListingsSold(), json.get("uniqueListingsSold"));
        assertEquals(record.getUniqueBuyers(), json.get("uniqueBuyers"));
        assertEquals(record.getUniqueProducts(), json.get("uniqueProducts"));
        assertEquals(record.getAverageLikeCount(), json.get("averageLikeCount"));
        assertEquals(record.getTotalPriceSold(), json.get("totalPriceSold"));
        assertEquals(record.getUniqueBuyers(), json.get("uniqueBuyers"));
        assertEquals(record.getAverageDaysToSell(), json.get("averageDaysToSell"));
    }

    @Test
    void construct_expectedUniqueListingsSold() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2, item3));
        assertEquals(3, record.getUniqueListingsSold());
    }

    @Test
    void construct_duplicateUsers_uniqueUserCountCorrect() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item3));
        assertEquals(1, record.getUniqueBuyers());
    }

    @Test
    void construct_noDuplicateUsers_uniqueUserCountCorrect() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2));
        assertEquals(2, record.getUniqueBuyers());
    }

    @Test
    void construct_duplicateProducts_uniqueProductCountCorrect() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item2, item3));
        assertEquals(1, record.getUniqueProducts());
    }

    @Test
    void construct_noDuplicateProducts_uniqueProductCountCorrect() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2));
        assertEquals(2, record.getUniqueProducts());
    }

    @Test
    void construct_expectedTotalQuantitySold() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2, item3));
        assertEquals(item1.getQuantity() + item2.getQuantity() + item3.getQuantity(), record.getTotalQuantitySold());
    }

    @Test
    void construct_expectedTotalPriceSold() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2, item3));
        assertEquals(item1.getPrice().add(item2.getPrice()).add(item3.getPrice()), record.getTotalPriceSold());
    }

    @Test
    void construct_notItemsProvided_averageLikeCountNull() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of());
        assertNull(record.getAverageLikeCount());
    }

    @Test
    void construct_itemsProvided_expectedAverageLikeCount() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2, item3));
        assertEquals(4.0, record.getAverageLikeCount());
    }

    @Test
    void construct_noItemsProvided_averageDaysToSellNull() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of());
        assertNull(record.getAverageDaysToSell());
    }

    @Test
    void construct_itemsProvided_expectedAverageDaysToSell() {
        var record = new BoughtSaleItemRecord(startDate, endDate, List.of(item1, item2));
        assertEquals(6.0, record.getAverageDaysToSell());
    }
}
