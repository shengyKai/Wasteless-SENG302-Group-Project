package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
