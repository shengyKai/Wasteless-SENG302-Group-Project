package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.search.SearchPageConstructor;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {
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
    private Product testProduct2;
    private Product testProduct3;
    private Product testProductNull;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @Captor
    ArgumentCaptor<Specification<InventoryItem>> specificationArgumentCaptor;
    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

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
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser)
                .build();
        Field businessIdField = Business.class.getDeclaredField("id");
        businessIdField.setAccessible(true);
        businessIdField.set(testBusiness, 1L);
        testProduct = new Product.Builder()
                .withProductCode("BEANS")
                .withBusiness(testBusiness)
                .withDescription("some description")
                .withManufacturer("manufacturer")
                .withName("some Name")
                .withRecommendedRetailPrice("15")
                .build();
        testProduct2 = new Product.Builder()
                .withProductCode("HAM")
                .withBusiness(testBusiness)
                .withDescription("some description 2")
                .withManufacturer("manufacturer 2")
                .withName("some Name 2")
                .withRecommendedRetailPrice("16")
                .build();
        testProduct3 = new Product.Builder()
                .withProductCode("VEGE")
                .withBusiness(testBusiness)
                .withDescription("another description")
                .withManufacturer("another manufacturer")
                .withName("another Name")
                .withRecommendedRetailPrice("17")
                .build();
        // this product will only have the bare minimum details to test null values for
        // sorting(some sort options are optional fields)
        testProductNull = new Product.Builder()
                .withProductCode("ZZZ")
                .withBusiness(testBusiness)
                .withName("zzz")
                .build();

        List<InventoryItem> inventory = new ArrayList<>();
        Page<InventoryItem> page = new PageImpl<>(inventory);
        addSeveralInventoryItemsToAnInventory(inventory);
        Business businessSpy = spy(testBusiness);
        when(businessSpy.getId()).thenReturn(1L);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @AfterEach
    void tearDown() {
        authenticationTokenManager.close();
    }

    /**
     * Generates a valid inventory item creation request body
     * @return JSONObject for a valid creation request
     */
    private JSONObject generateInventoryCreateInfo() {
        var inventory = new JSONObject();
        inventory.put("productId", "BEANS");
        inventory.put("quantity", 4);
        inventory.put("pricePerItem", 6.5);
        inventory.put("totalPrice", 21.99);
        inventory.put("manufactured", "2021-05-12");
        inventory.put("sellBy",     LocalDate.now().plus(10, ChronoUnit.DAYS).toString());
        inventory.put("bestBefore", LocalDate.now().plus(20, ChronoUnit.DAYS).toString());
        inventory.put("expires",    LocalDate.now().plus(30, ChronoUnit.DAYS).toString());
        return inventory;
    }

    @Test
    void addInventory_validPermission_canAddInventory() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isOk());
    }


    @Test
    void addInventory_normalUser_cannotAddInventory403() throws Exception {
        Business businessSpy = spy(testBusiness);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        sessionAuthToken.put("accountId", 1L); // use a random account

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString())
                .sessionAttrs(sessionAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void addInventory_notLoggedIn_cannotAddInventory401() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());
        Business businessSpy = spy(testBusiness);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        doCallRealMethod().when(businessSpy).checkSessionPermissions(any());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isUnauthorized());

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addInventory_invalidBusinessId_406Thrown() throws Exception {
        when(businessRepository.getBusinessById(any())).thenCallRealMethod();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/999/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_noRequestBody_400Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy); // use our product
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
        when(productRepository.getProduct(any(), any())).thenCallRealMethod(); // use real method
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_productOfWrongBusiness_403Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        Optional<Product> optionalProduct = Optional.of(productSpy);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProduct(any(), any())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)); // make this method always throw
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isForbidden());
    }


    @Test
    void addInventory_inventoryCreated_inventorySavedToDatabase() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isOk());

        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));

    }

    @Test
    void getInventory_unverifiedAccessToken_401Thrown() throws Exception {
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getInventory_insufficientPermissions_403Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getInventory_businessNotFound_406Thrown() {
        when(businessRepository.getBusinessById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getInventory_emptyInventory_emptyPageReturned() {
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        var result = inventoryController.getInventory(1L, request, null, null, null, null);

        verify(inventoryItemRepository, times(1)).findAll(any(), any(PageRequest.class));

        assertEquals(0L, result.getResults().size());
        assertEquals(0L, result.getCount());
    }


    @Test
    void getInventory_inventoryWithItems_pageWithItemsReturned() throws Exception {
        String futureDate = LocalDate.now().plus(50, ChronoUnit.DAYS).toString();
        List<InventoryItem> items = List.of(
                new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires(futureDate).build(),
                new InventoryItem.Builder().withProduct(testProduct).withQuantity(39).withExpires(futureDate).build(),
                new InventoryItem.Builder().withProduct(testProduct).withQuantity(54).withExpires(futureDate).build()
        );

        Page<InventoryItem> inventory = new PageImpl<>(items, Pageable.unpaged(), 1000L);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(inventory);
        var result = inventoryController.getInventory(1L, request, null, null, null, null);
        
        JSONArray expectedArray = new JSONArray();
        items.stream().map(InventoryItemResponseDTO::new).forEach(expectedArray::add);
        
        assertEquals(expectedArray, result.getResults());
        assertEquals(1000L, result.getCount());
    }


    @Test
    void retrievePaginatedInventory_firstPage_firstRequested() throws Exception {
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("page", "1").param("resultsPerPage", "2"))
                .andExpect(status().isOk()).andReturn();

        Specification<InventoryItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromInventoryItemsFilter(testBusiness);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(1, 2, Sort.by(new Sort.Order(Direction.ASC, "product.productCode").ignoreCase()));

        verify(inventoryItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void retrievePaginatedInventory_secondPage_secondPageRequested() throws Exception {
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("page", "2").param("resultsPerPage", "2"))
                .andExpect(status().isOk()).andReturn();

        Specification<InventoryItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromInventoryItemsFilter(testBusiness);

        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(2, 2, Sort.by(new Sort.Order(Direction.ASC, "product.productCode").ignoreCase()));

        verify(inventoryItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @ParameterizedTest
    @CsvSource({
            ",product.productCode",
            "productCode,product.productCode",
            "name,product.name",
            "description,product.description",
            "manufacturer,product.manufacturer",
            "recommendedRetailPrice,product.recommendedRetailPrice",
            "created,product.created",
            "quantity,quantity",
            "pricePerItem,pricePerItem",
            "totalPrice,totalPrice",
            "sellBy,sellBy",
            "bestBefore,bestBefore",
            "expires,expires",
    })
    void retrieveSortedInventory_byProvidedField_requestedOrderingByField(String orderBy, String ordering) throws Exception {
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", orderBy))
                .andExpect(status().isOk()).andReturn();

        Specification<InventoryItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromInventoryItemsFilter(testBusiness);

        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Direction.ASC, ordering).ignoreCase()));

        verify(inventoryItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void retrieveSortedInventory_reversed_reverseOrderingRequested() throws Exception {
        when(inventoryItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("reverse", "true").param("orderBy", "name"))
                .andExpect(status().isOk()).andReturn();

        Specification<InventoryItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromInventoryItemsFilter(testBusiness);

        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Direction.DESC, "product.name").ignoreCase()));

        verify(inventoryItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    /**
     * Creates several inventory items based on a product. These items have
     * differing attributes to identify them.
     * 
     * @throws Exception Exception
     */
    public void addSeveralInventoryItemsToAnInventory(List<InventoryItem> inventory) throws Exception {
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2028-01-01")
                .withPricePerItem("1").withTotalPrice("1").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(2).withExpires("2028-01-01")
                .withPricePerItem("2").withTotalPrice("2").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(3).withExpires("2029-01-01")
                .withPricePerItem("3").withTotalPrice("3").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(4).withExpires("2029-01-01")
                .withPricePerItem("4").withTotalPrice("4").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(5).withExpires("2030-06-06")
                .withPricePerItem("5").withTotalPrice("5").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(6).withExpires("2030-06-06")
                .withPricePerItem("6").withTotalPrice("6").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        // inventory item with the bare minimum to exist as an inventory item
        inventory.add(new InventoryItem.Builder().withProduct(testProductNull).withQuantity(7).withExpires("2031-06-06")
                .build());
    }
}
