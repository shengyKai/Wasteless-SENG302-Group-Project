package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.entities.*;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MvcResult;
import net.minidev.json.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private InventoryController inventoryController;
    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();

    @Mock
    private HttpServletRequest request;
    @MockBean
    private BusinessRepository businessRepository;
    @MockBean
    private InventoryItemRepository inventoryItemRepository;
    @MockBean
    private ProductRepository productRepository;

    private User testUser;
    @Mock
    private Business testBusiness;
    @Mock
    private Business mockBusiness;
    @Mock
    private List<Product> mockProductList;
    @Mock
    private HttpSession session;
    private Product testProduct;

    @BeforeEach
    public void setup() throws ParseException {
        MockitoAnnotations.openMocks(this);

        testUser = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Elliot")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser)
                .build();
        testProduct = new Product.Builder()
                .withProductCode("BEANS")
                .withBusiness(testBusiness)
                .withDescription("some description")
                .withManufacturer("manufacturer")
                .withName("some Name")
                .withRecommendedRetailPrice("15")
                .build();
    }

    @Test
    void addInventory_validPermission_canAddInventory() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-12-12\",\n" +
                "  \"bestBefore\": \"2021-12-12\",\n" +
                "  \"expires\": \"2021-12-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isOk());
    }


    @Test
    void addInventory_normalUser_cannotAddInventory403() throws Exception {
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager
                    .when(() -> AuthenticationTokenManager
                            .checkAuthenticationToken(any()))
                    .then(invocation -> null); // mock a valid session
            Business businessSpy = spy(testBusiness);
            when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
            sessionAuthToken.put("accountId", 1L); // use a random account

            String inventory = "{\n" +
                    "  \"productId\": \"BEANS\",\n" +
                    "  \"quantity\": 4,\n" +
                    "  \"pricePerItem\": 6.5,\n" +
                    "  \"totalPrice\": 21.99,\n" +
                    "  \"manufactured\": \"2021-05-12\",\n" +
                    "  \"sellBy\": \"2021-05-12\",\n" +
                    "  \"bestBefore\": \"2021-05-12\",\n" +
                    "  \"expires\": \"2021-05-12\"\n" +
                    "}";
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/businesses/1/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inventory)
                    .sessionAttrs(sessionAuthToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void addInventory_notLoggedIn_cannotAddInventory401() throws Exception {
        Business businessSpy = spy(testBusiness);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addInventory_invalidBusinessId_406Thrown() throws Exception {
        when(businessRepository.getBusinessById(any())).thenCallRealMethod();
        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/999/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_noRequestBody_400Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication


        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addInventory_productNotExist_406Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenCallRealMethod(); // use real method
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_productOfWrongBusiness_403Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        Optional<Product> optionalProduct = Optional.of(productSpy);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)); // make this method always throw
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isForbidden());
    }


    @Test
    void addInventory_inventoryCreated_inventorySavedToDatabase() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication


        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-12-12\",\n" +
                "  \"bestBefore\": \"2021-12-12\",\n" +
                "  \"expires\": \"2021-12-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isOk());

        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));

    }

    @Test
    void getInventory_unverifiedAccessToken_401Thrown() throws Exception {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new AccessTokenException()).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventory(1L, request, "", "", "", ""));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getInventoryCount_unverifiedAccessToken_401Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new AccessTokenException()).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getInventory_insufficientPermissions_403Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventory(1L, request, "", "", "", ""));
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getInventoryCount_insufficientPermissions_403Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getInventory_businessNotFound_406Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventory(1L, request, "", "", "", ""));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getInventoryCount_businessNotFound_406Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getInventory_emptyInventory_emptyArrayReturned() {
        List<InventoryItem> emptyInventory = new ArrayList<>();
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(emptyInventory);
        JSONArray result = inventoryController.getInventory(1L, request, "", "", "", "");
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void getInventoryCount_emptyInventory_zeroReturned() {
        List<InventoryItem> emptyInventory = new ArrayList<>();
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(emptyInventory);
        JSONObject result = inventoryController.getInventoryCount(1L, request);
        Assertions.assertTrue(result.containsKey("count"));
        Assertions.assertEquals(0, result.getAsNumber("count"));
    }

    @Test
    void getInventory_multipleItems_correctArrayReturned() throws Exception {
        List<InventoryItem> inventory = new ArrayList<>();
        JSONArray expectedResponse = new JSONArray();
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(39).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(54).withExpires("2022-01-01").build());
        for (InventoryItem item : inventory) {
            expectedResponse.add(item.constructJSONObject());
        }
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(inventory);
        JSONArray result = inventoryController.getInventory(1L, request, "", "", "", "");
        Assertions.assertEquals(expectedResponse, result);
    }

    @Test
    void getInventoryCount_multipleItems_correctCountReturned() throws Exception {
        List<InventoryItem> inventory = new ArrayList<>();
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(39).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(54).withExpires("2022-01-01").build());
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(inventory);
        JSONObject result = inventoryController.getInventoryCount(1L, request);
        Assertions.assertTrue(result.containsKey("count"));
        Assertions.assertEquals(3, result.getAsNumber("count"));
    }

    /**
     * Checks that the API returns the first page of paginated products in the businesses catalogue.
     */
    @Test
    void retrievePaginatedInventoryFirstPage() throws Exception {
        List<InventoryItem> inventory = new ArrayList<>();
        addSeveralInventoryItemsToAnInventory(inventory);
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(inventory);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("resultsPerPage", "2"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        // Check length should be 2 products
        assertEquals(2, responseBody.size());

        // Check the two products are the expected ones
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);

        assertEquals("1", firstInventory.getAsString("quantity"));
        assertEquals("2", secondInventory.getAsString("quantity"));
    }

    /**
     * Creates several inventory items based on a product.
     * @throws Exception
     */
    public void addSeveralInventoryItemsToAnInventory(List<InventoryItem> inventory) throws Exception {
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(2).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(3).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(4).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(5).withExpires("2022-01-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(6).withExpires("2022-01-01").build());
    }
}



