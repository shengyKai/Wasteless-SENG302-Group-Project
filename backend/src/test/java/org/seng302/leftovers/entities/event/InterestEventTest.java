package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.SaleItemDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterestEventTest {
    @Autowired
    private ObjectMapper mapper;

    @Mock
    private User user;

    @Mock
    private SaleItem saleItem;
    @Mock
    private Product product;
    @Mock
    private Business business;
    @Mock
    private Location businessAddress;
    @Mock
    private User businessPrimaryOwner;

    @Mock
    private InventoryItem inventoryItem;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(saleItem.getInventoryItem()).thenReturn(inventoryItem);
        when(inventoryItem.getProduct()).thenReturn(product);
        when(product.getBusiness()).thenReturn(business);
        when(business.getAddress()).thenReturn(businessAddress);
        when(business.getPrimaryOwner()).thenReturn(businessPrimaryOwner);
    }

    @Test
    void asDTO_correctJson() {
        var event = spy(new InterestEvent(user, saleItem));
        when(event.getId()).thenReturn(30L);

        event.setInterested(false);

        var json = mapper.convertValue(event.asDTO(), JSONObject.class);
        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("InterestEvent", json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals(event.getStatus().toString().toLowerCase(), json.get("status"));
        assertEquals(event.isRead(), json.get("read"));
        assertEquals(event.getInterested(), json.get("interested"));

        var actualSaleItem = mapper.convertValue(json.get("saleItem"), SaleItemDTO.class);
        assertEquals(event.getSaleItem().getId(), actualSaleItem.getId());
        assertEquals(event.getLastModified().toString(), json.getAsString("lastModified"));
        assertEquals(9, json.size());
    }
}
