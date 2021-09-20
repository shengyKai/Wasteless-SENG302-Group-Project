package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemDTO;
import org.seng302.leftovers.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InterestPurchasedEventTest {
    @Autowired
    private ObjectMapper mapper;

    @Mock
    private User user;

    @Mock
    private BoughtSaleItem boughtSaleItem;
    @Mock
    private Product product;
    @Mock
    private Business business;
    @Mock
    private Location businessAddress;
    @Mock
    private User businessPrimaryOwner;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(boughtSaleItem.getProduct()).thenReturn(product);
        when(product.getBusiness()).thenReturn(business);
        when(business.getAddress()).thenReturn(businessAddress);
        when(business.getPrimaryOwner()).thenReturn(businessPrimaryOwner);
    }

    @Test
    void asDTO_correctJson() {
        var event = spy(new InterestPurchasedEvent(user, boughtSaleItem));
        when(event.getId()).thenReturn(30L);

        var json = mapper.convertValue(event.asDTO(), JSONObject.class);
        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("InterestPurchasedEvent", json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals(event.getStatus().toString().toLowerCase(), json.get("status"));
        assertEquals(event.isRead(), json.get("read"));

        var actualBoughtSaleItem = mapper.convertValue(json.get("boughtSaleItem"), BoughtSaleItemDTO.class);
        assertEquals(null, actualBoughtSaleItem.getBuyer());
        assertEquals(event.getBoughtSaleItem().getId(), actualBoughtSaleItem.getId());
        assertEquals(event.getLastModified().toString(), json.getAsString("lastModified"));
        assertEquals(8, json.size());
    }
}
