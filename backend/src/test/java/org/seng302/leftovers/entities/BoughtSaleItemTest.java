package org.seng302.leftovers.entities;


import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoughtSaleItemTest {
    @Mock
    private Product product;
    @Mock
    private User user;
    @Mock
    private SaleItem saleItem;

    @BeforeEach
    void setup() {
        when(saleItem.getId()).thenReturn(3L);
        when(saleItem.getProduct()).thenReturn(product);
        when(saleItem.getPrice()).thenReturn(new BigDecimal("100"));
        when(saleItem.getQuantity()).thenReturn(6);
        when(saleItem.getCreated()).thenReturn(Instant.parse("2021-09-14T03:07:30.713Z"));
        when(saleItem.getLikeCount()).thenReturn(5);
    }

    @Test
    void new_withSaleItem_hasSaleItemsFields() {
        var boughtSaleItem = new BoughtSaleItem(saleItem, user);

        assertEquals(user, boughtSaleItem.getBuyer());
        assertEquals(saleItem.getProduct(), boughtSaleItem.getProduct());
        assertEquals(saleItem.getPrice(), boughtSaleItem.getPrice());
        assertEquals(saleItem.getQuantity(), boughtSaleItem.getQuantity());
        assertEquals(saleItem.getCreated(), boughtSaleItem.getListingDate());
        assertEquals(saleItem.getLikeCount(), boughtSaleItem.getLikeCount());
    }

    @Test
    void new_withSaleItem_saleDateIsValid() {
        var before = Instant.now();
        var boughtSaleItem = new BoughtSaleItem(saleItem, user);
        var after = Instant.now();

        assertFalse(boughtSaleItem.getSaleDate().isBefore(before));
        assertFalse(boughtSaleItem.getSaleDate().isAfter(after));
    }
}
