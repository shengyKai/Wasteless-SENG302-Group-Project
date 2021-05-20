package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.seng302.entities.*;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        when(inventoryItem.getExpires()).thenReturn(LocalDate.now());
        when(inventoryItem.getBusiness()).thenReturn(business);

        when(inventoryItemRepository.getInventoryItemByBusinessAndId(any(Business.class), anyLong())).thenCallRealMethod();
        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());

        // Setup mock sale item repository
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(x -> x.getArgument(0));

        var controller = new SaleController(businessRepository, saleItemRepository, inventoryItemRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private JSONObject generateSalesItemInfo() {
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
        assertEquals(new BigDecimal(object.getAsString("price")), saleItem.getPrice());
        assertEquals(object.get("moreInfo"), saleItem.getMoreInfo());
        assertEquals(object.getAsString("closes"), saleItem.getCloses().toString());
    }

    @Test
    void addSaleItemToBusiness_validInput_ReturnsId() throws Exception {
        SaleItem mockItem = mock(SaleItem.class);
        when(mockItem.getSaleId()).thenReturn(400L);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(mockItem);

        var object = generateSalesItemInfo();
        MvcResult result = mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject actualResponse = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        JSONObject expectedResponse = new JSONObject();
        expectedResponse.put("listingId", 400);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getSaleItemsForBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void getSaleItemsForBusiness_invalidBusiness_406Response() throws Exception {
        mockMvc.perform(get("/businesses/999/listings"))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    @Test
    void getSaleItemsForBusiness_validBusiness_doesNotCheckSessionPermissions() throws Exception {
        mockMvc.perform(get("/businesses/1/listings"))
                .andReturn();

        verify(business, times(0)).checkSessionPermissions(any(HttpServletRequest.class));
    }

    @Test
    void getSaleItemsForBusiness_validBusinessNoSalesItem_returnsEmptyList() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        assertEquals(new JSONArray(), response);
    }

    @Test
    void getSaleItemsForBusiness_validBusinessNoSales_returnsSaleItems() throws Exception {
        when(business.getAddress()).thenReturn(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"));
        Product product = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(business)
                .build();

        InventoryItem inventoryItem = new InventoryItem.Builder()
                .withProduct(product)
                .withQuantity(300)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy("2021-05-21")
                .withBestBefore("2021-05-28")
                .withExpires("2021-06-01")
                .build();


        List<SaleItem> saleItems = new ArrayList<>();
        saleItems.add(new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build());
        saleItems.add(new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This shouldn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build());


        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(Collections.unmodifiableList(saleItems));
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (SaleItem item : saleItems) {
            expected.add(item.constructJSONObject());
        }
        // This will normalise out the big decimals into doubles and such
        expected = (JSONArray) parser.parse(expected.toString());

        assertEquals(expected, response);
    }
}
