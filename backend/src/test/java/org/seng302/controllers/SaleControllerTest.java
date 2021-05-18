package org.seng302.controllers;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.SaleItem;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.*;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class SaleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private Business business;
    @Mock
    private InventoryItem inventoryItem;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Setup mock business
        when(business.getId()).thenReturn(1L);

        when(businessRepository.getBusinessById(1L)).thenReturn(business);
        when(businessRepository.getBusinessById(not(eq(1L)))).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        // Setup mock inventory item
        when(inventoryItem.getId()).thenReturn(2L);
        when(inventoryItem.getBusiness()).thenReturn(business);

        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());

        var controller = new SaleController(businessRepository, saleItemRepository, inventoryItemRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private JSONObject generateSalesItemInfo() throws JSONException {
        var object = new JSONObject();
        object.put("inventoryItemId", 2);
        object.put("quantity", 3);
        object.put("price", 10.5);
        object.put("moreInfo", "This is some more info about the product");
        object.put("closes", "2021-07-21");
        return object;
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    @Test
    void addSaleItemToBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                    .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the POST request
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addSaleItemToBusiness_validAuthToken_not401Response() throws Exception {
        mockMvc.perform(post("/businesses/1/listings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateSalesItemInfo().toString()))
                    .andExpect(status().is(Matchers.not(401)))
                    .andReturn();
        // Checks that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addSaleItemToBusiness_businessDoesNotExist_406Response() throws Exception {
        mockMvc.perform(post("/businesses/100/listings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateSalesItemInfo().toString()))
                    .andExpect(status().isNotAcceptable())
                    .andReturn();
    }

    @Test
    void addSaleItemToBusiness_cannotActAsBusiness_403Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(business).checkSessionPermissions(any());

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(business).checkSessionPermissions(any());
    }

    @Test
    void addSaleItemToBusiness_noRequestBody_400Response() throws Exception {
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemIdNotProvided_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.remove("inventoryItemId");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemIdNotNumber_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("inventoryItemId", "seven");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemNotFound_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("inventoryItemId", 100);

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemDifferentBusiness_400Response() throws Exception {
        var otherBusiness = mock(Business.class);
        when(otherBusiness.getId()).thenReturn(100L);

        when(inventoryItem.getBusiness()).thenReturn(otherBusiness);

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_quantityNotProvided_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.remove("quantity");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_quantityNotInteger_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("quantity", "seven");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_validInput_201Response() throws Exception {
        var object = generateSalesItemInfo();
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<SaleItem> captor = ArgumentCaptor.forClass(SaleItem.class);
        verify(saleItemRepository).save(captor.capture());
        SaleItem saleItem = captor.getValue();

        assertSame(inventoryItem, saleItem.getInventoryItem());
        assertEquals(object.get("quantity"), saleItem.getQuantity());
        assertEquals(new BigDecimal(object.getString("price")), saleItem.getPrice());
        assertEquals(object.get("moreInfo"), saleItem.getMoreInfo());
        assertEquals(object.getString("closes"), saleItem.getCloses().toString());
    }
}
