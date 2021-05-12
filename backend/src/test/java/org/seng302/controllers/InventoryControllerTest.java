package org.seng302.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private InventoryController inventoryController;

    @Mock
    private HttpServletRequest request;
    @Mock
    BusinessRepository businessRepository;
    @Mock
    InventoryItemRepository inventoryItemRepository;
    @Mock
    ProductRepository productRepository;

    @BeforeEach
    public void setup() throws ParseException {
        MockitoAnnotations.openMocks(this);
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
    }


    @Test
    void addInventory_businessOwner_canAddInventory() {
    }

    @Test
    void addInventory_businessAdmin_canAddInventory() {

    }

    @Test
    void addInventory_normalUser_cannotAddInventory() {

    }

    @Test
    void addInventory_sysAdmin_canAddInventory() {

    }

    @Test
    void addInventory_invalidBusinessId_406Thrown() {

    }

    @Test
    void addInventory_noRequestBody_401Thrown() {

    }

    @Test
    void addInventory_productNotExist_406Thrown() {

    }

    @Test
    void addInventory_productOfWrongBusiness_403Thrown() {

    }

    @Test
    void addInventory_validProductDetails_canCreateInventory() {

    }

    @Test
    void addInventory_inventoryCreated_inventorySavedToDatabase() {

    }

}



