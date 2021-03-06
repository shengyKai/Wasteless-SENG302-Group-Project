package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.saleitem.BoughtSaleItemRecord;
import org.seng302.leftovers.dto.saleitem.ReportGranularity;
import org.seng302.leftovers.dto.saleitem.SaleListingSearchDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.entities.event.InterestPurchasedEvent;
import org.seng302.leftovers.entities.event.PurchasedEvent;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.seng302.leftovers.service.ReportService;
import org.seng302.leftovers.service.search.SearchQueryParser;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.search.SearchPageConstructor;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private InterestEventRepository interestEventRepository;
    @Mock
    private BoughtSaleItemRepository boughtSaleItemRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ReportService reportService;
    @Mock
    private Business business;
    @Mock
    private User user;
    @Mock
    private User interestedUser1;
    @Mock
    private User interestedUser2;
    @Mock
    private User interestedUser3;
    @Mock
    private InventoryItem inventoryItem;
    @Mock
    private SaleItem saleItem;
    @Mock
    private User businessPrimaryOwner;
    @Mock
    private Location businessAddress;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private SaleController saleController;

    @Captor
    ArgumentCaptor<Specification<SaleItem>> specificationArgumentCaptor;
    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;
    @Captor
    ArgumentCaptor<Event> eventCaptor;

    private MockedStatic<SearchPageConstructor> searchPageConstructor;
    private MockedStatic<SearchSpecConstructor> searchSpecConstructor;


    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        searchPageConstructor = Mockito.mockStatic(SearchPageConstructor.class);
        searchSpecConstructor = Mockito.mockStatic(SearchSpecConstructor.class);

        searchPageConstructor.when(() -> SearchPageConstructor.getSortDirection(any())).thenReturn(Sort.Direction.DESC);
        searchPageConstructor.when(() -> SearchPageConstructor.getPageRequest(any(), any(), any()))
                .thenReturn(PageRequest.of(1, 1, Sort.unsorted()));
        searchSpecConstructor.when(() -> SearchSpecConstructor.constructSaleListingSpecificationForSearch(any()))
                .thenReturn(Specification.where(null));
        searchSpecConstructor.when(() -> SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(any()))
                .thenReturn(Specification.where(null));

        // Setup mock business
        when(business.getId()).thenReturn(1L);
        when(business.getAddress()).thenReturn(businessAddress);
        when(business.getPrimaryOwner()).thenReturn(businessPrimaryOwner);
        when(business.getCreated()).thenReturn(Instant.parse("2021-09-08T08:47:59Z"));

        when(businessRepository.getBusinessById(1L)).thenReturn(business);
        when(businessRepository.getBusinessById(not(eq(1L)))).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        // Setup mock inventory item
        when(inventoryItem.getId()).thenReturn(2L);
        when(inventoryItem.getExpires()).thenReturn(LocalDate.now());
        when(inventoryItem.getBusiness()).thenReturn(business);

        when(inventoryItemRepository.findInventoryItemByBusinessAndId(any(Business.class), anyLong())).thenCallRealMethod();
        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());

        when(saleItem.getInventoryItem()).thenReturn(inventoryItem);

        // Setup mock sale item repository
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(x -> x.getArgument(0));
        when(saleItemRepository.findById(not(eq(3L)))).thenReturn(Optional.empty());
        when(saleItemRepository.findById(3L)).thenReturn(Optional.of(saleItem));

        when(saleItem.getId()).thenReturn(3L);

        // Setup mock user
        when(user.getUserID()).thenReturn(4L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.findById(not(eq(4L)))).thenReturn(Optional.empty());

        saleController = spy(new SaleController(userRepository, businessRepository, saleItemRepository,
                inventoryItemRepository, interestEventRepository, boughtSaleItemRepository, eventRepository, reportService, objectMapper));
        mockMvc = MockMvcBuilders.standaloneSetup(saleController).build();
    }

    private JSONObject generateSalesItemInfo() {
        var object = new JSONObject();
        object.put("inventoryItemId", 2);
        object.put("quantity", 3);
        object.put("price", 10.5);
        object.put("moreInfo", "This is some more info about the product");
        object.put("closes", LocalDate.now().plus(100, ChronoUnit.DAYS).toString());
        return object;
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
        searchPageConstructor.close();
        searchSpecConstructor.close();
    }

    @Test
    void addSaleItemToBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                    .thenThrow(new AccessTokenResponseException());

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
        when(mockItem.getId()).thenReturn(400L);
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
    void addSaleItemToBusiness_validInput_businessPointsUpdated() throws Exception {
        var object = generateSalesItemInfo();
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        verify(business, times(1)).incrementPoints();
        verify(businessRepository, times(1)).save(business);
    }

    @Test
    void getSaleItemsForBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

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
    void getSaleItemsForBusiness_validBusinessNoSalesItem_returnsEmptyList() throws Exception {
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONObject expected = new JSONObject();
        expected.appendField("count", 0);
        expected.appendField("results", new JSONArray());

        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsForBusiness_withSortOrder_usesSortOrder() throws Exception {
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/businesses/1/listings")
                .param("orderBy", "price"))
                .andReturn();
        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "price").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_noSortOrder_usesCreatedSortOrder() throws Exception {
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/businesses/1/listings"))
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    /**
     * Generates a consistently shuffled list of mock sale items
     * @return Mock sale item list
     */
    List<SaleItem> generateMockSaleItems() {
        List<SaleItem> mockItems = new ArrayList<>();
        for (long i = 0; i<6; i++) {
            var saleItem = mock(SaleItem.class);
            var product = mock(Product.class);
            when(saleItem.getId()).thenReturn(i);
            var inventoryItem = mock(InventoryItem.class);
            when(saleItem.getInventoryItem()).thenReturn(inventoryItem);
            when(inventoryItem.getProduct()).thenReturn(product);
            when(product.getBusiness()).thenReturn(business);
            mockItems.add(saleItem);
        }
        // Ensure determinism
        Collections.shuffle(mockItems, new Random(7));
        return mockItems;
    }

    @Test
    void getSaleItemsForBusiness_noReverse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_reverseFalse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        saleItemRepository.saveAll(items);

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_reverseTrue_itemsDescending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        saleItemRepository.saveAll(items);


        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "true"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.DESC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_resultsPerPageSet_firstPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(null, 4, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_secondPageRequested_secondPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        saleItemRepository.saveAll(items);


        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4")
                .param("page","2"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchPageConstructor.getPageRequest(2, 4, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    private JSONObject createUpdateInterestRequest(long userId, boolean interested) {
        var json = new JSONObject();
        json.put("userId", userId);
        json.put("interested", interested);
        return json;
    }

    @Test
    void setSaleItemInterest_notLoggedIn_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        // Verify that a 401 response is received in response to the PUT request
        mockMvc.perform(put("/listings/1/interest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isUnauthorized());

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(saleItemRepository, times(0)).save(any());
    }

    @Test
    void setSaleItemInterest_cannotUpdateUser_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);

        // Verify that a 403 response is received in response to the PUT request
        mockMvc.perform(put("/listings/1/interest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isForbidden());

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(4L)));
        verify(saleItemRepository, times(0)).save(any());
    }

    @Test
    void setSaleItemInterest_invalidRequest_400Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        var request = createUpdateInterestRequest(4, true);
        request.remove("userId");

        // Verify that a 400 response is received in response to the PUT request
        mockMvc.perform(put("/listings/1/interest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isBadRequest());

        verify(saleItemRepository, times(0)).save(any());
    }

    @Test
    void setSaleItemInterest_userDoesNotExist_400Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);


        // Verify that a 401 response is received in response to the PUT request
        mockMvc.perform(put("/listings/1/interest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(9999, true).toString()))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(1)).findById(9999L);
        verify(saleItemRepository, times(0)).save(any());
    }

    @Test
    void setSaleItemInterest_listingDoesNotExist_406Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);


        // Verify that a 406 response is received in response to the PUT request
        mockMvc.perform(put("/listings/9999/interest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isNotAcceptable());

        verify(saleItemRepository, times(1)).findById(9999L);
        verify(saleItemRepository, times(0)).save(any());
    }

    @Test
    void setSaleItemInterest_setInterested_200ResponseAndUserAdded() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Verify that a 200 response is received in response to the PUT request
        mockMvc.perform(put(String.format("/listings/%s/interest", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isOk());

        verify(saleItem, times(1)).addInterestedUser(user);

        verify(saleItemRepository, times(1)).findById(saleItem.getId());
        verify(saleItemRepository, times(1)).save(saleItem);
    }

    @Test
    void setSaleItemInterest_setUnInterested_200ResponseAndUserRemoved() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Verify that a 401 response is received in response to the PUT request
        mockMvc.perform(put(String.format("/listings/%s/interest", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, false).toString()))
                .andExpect(status().isOk());

        verify(saleItem, times(1)).removeInterestedUser(user);

        verify(saleItemRepository, times(1)).findById(3L);
        verify(saleItemRepository, times(1)).save(saleItem);
    }

    @Test
    void setSaleItemInterest_noInterestEventExists_newInterestEventCreated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Verify that a 200 response is received in response to the PUT request
        mockMvc.perform(put(String.format("/listings/%s/interest", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isOk());

        verify(interestEventRepository, times(1)).findInterestEventByNotifiedUserAndSaleItem(user, saleItem);

        var captor = ArgumentCaptor.forClass(InterestEvent.class);

        verify(interestEventRepository, times(1)).save(captor.capture());
        var interestEvent = captor.getValue();
        assertTrue(interestEvent.getInterested());
        assertEquals(user, interestEvent.getNotifiedUser());
        assertEquals(saleItem, interestEvent.getSaleItem());
    }

    @Test
    void setSaleItemInterest_likeAndInterestEventExists_interestEventUpdated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        var interestEvent = mock(InterestEvent.class);
        when(interestEventRepository.findInterestEventByNotifiedUserAndSaleItem(user, saleItem)).thenReturn(Optional.of(interestEvent));

        // Verify that a 200 response is received in response to the PUT request
        mockMvc.perform(put(String.format("/listings/%s/interest", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, true).toString()))
                .andExpect(status().isOk());

        verify(interestEventRepository, times(1)).findInterestEventByNotifiedUserAndSaleItem(user, saleItem);

        verify(interestEvent, times(1)).setInterested(true);

        verify(interestEventRepository, times(1)).save(interestEvent);
    }

    @Test
    void setSaleItemInterest_unlikeAndInterestEventExists_interestEventUpdated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        var interestEvent = mock(InterestEvent.class);
        when(interestEventRepository.findInterestEventByNotifiedUserAndSaleItem(user, saleItem)).thenReturn(Optional.of(interestEvent));

        // Verify that a 200 response is received in response to the PUT request
        mockMvc.perform(put(String.format("/listings/%s/interest", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInterestRequest(4, false).toString()))
                .andExpect(status().isOk());

        verify(interestEventRepository, times(1)).findInterestEventByNotifiedUserAndSaleItem(user, saleItem);

        verify(interestEvent, times(1)).setInterested(false);

        verify(interestEventRepository, times(1)).save(interestEvent);
    }

    @Test
    void saleSearch_noQuery_200() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));

        mockMvc.perform(get("/businesses/listings/search"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"true,true", "false,false"})
    void saleSearch_reverseQuery_PageConstructorCalledExpected(String input, Boolean reverse) throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        ArgumentCaptor<Boolean> reverseArgCaptor = ArgumentCaptor.forClass(Boolean.class);
        searchPageConstructor.when(() -> SearchPageConstructor.getSortDirection(reverseArgCaptor.capture())).thenCallRealMethod();
        mockMvc.perform(get("/businesses/listings/search").param("reverse", input))
                    .andExpect(status().isOk());
        assertEquals(reverse, reverseArgCaptor.getValue());
    }

    @Test
    void saleSearch_pageRequest_PageConstructorCalledExpected() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));
        ArgumentCaptor<Integer> pageArgCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> resultNumArgCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Sort> orderArgCaptor = ArgumentCaptor.forClass(Sort.class);

        searchPageConstructor.when(() -> SearchPageConstructor.getPageRequest(pageArgCaptor.capture(), resultNumArgCaptor.capture(), orderArgCaptor.capture())).thenReturn(PageRequest.of(1, 1, Sort.unsorted()));

        mockMvc.perform(get("/businesses/listings/search")
                .param("page", "5")
                .param("resultsPerPage", "12")
                .param("orderBy", "productName"))
                .andExpect(status().isOk());

        Sort expectedSort = Sort.by(new Sort.Order(Sort.Direction.DESC, "inventoryItem.product.name").ignoreCase().nullsLast());
        assertEquals(5, pageArgCaptor.getValue());
        assertEquals(12, resultNumArgCaptor.getValue());
        assertEquals(expectedSort, orderArgCaptor.getValue());
    }

    @Test
    void saleSearch_severalParameters_200() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(items));

        ArgumentCaptor<SaleListingSearchDTO> specArgCaptor = ArgumentCaptor.forClass(SaleListingSearchDTO.class);
        searchSpecConstructor.when(() -> SearchSpecConstructor.constructSaleListingSpecificationForSearch(specArgCaptor.capture())).thenReturn(Specification.where(null));

        mockMvc.perform(get("/businesses/listings/search")
                .param("productSearchQuery", "Cheese")
                .param("businessSearchQuery", "Lactose and Co")
                .param("locationSearchQuery", "Here")
                .param("priceLower", "2.00")
                .param("priceUpper", "25.00")
                .param("closeLower", "2022-09-09")
                .param("businessTypes", "Retail Trade"))
                .andExpect(status().isOk());

        assertEquals("Cheese", specArgCaptor.getValue().getProductSearchQuery());
        assertEquals("Lactose and Co", specArgCaptor.getValue().getBusinessSearchQuery());
        assertEquals("Here", specArgCaptor.getValue().getLocationSearchQuery());
        assertEquals(new BigDecimal("2.00"), specArgCaptor.getValue().getPriceLowerBound());
        assertEquals(new BigDecimal("25.00"), specArgCaptor.getValue().getPriceUpperBound());
        assertEquals(LocalDate.parse("2022-09-09"), specArgCaptor.getValue().getClosingDateLowerBound());
        assertEquals(List.of(BusinessType.RETAIL_TRADE), specArgCaptor.getValue().getBusinessTypes());
        assertNull(specArgCaptor.getValue().getBasicSearchQuery());
        assertNull(specArgCaptor.getValue().getClosingDateUpperBound());
    }

    @Test
    void saleSearch_invalidSearchParametersType_400() throws Exception {
        mockMvc.perform(get("/businesses/listings/search")
                .param("businessTypes", "Meat Farm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saleSearch_invalidSearchParametersPrice_400() throws Exception {
        mockMvc.perform(get("/businesses/listings/search")
                .param("priceUpper", "Meat Farm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saleSearch_invalidSearchParametersClose_400() throws Exception {
        mockMvc.perform(get("/businesses/listings/search")
                .param("closeUpper", "Meat Farm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saleSearch_notLoggedIn_401() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        mockMvc.perform(get("/businesses/listings/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSaleItemsInterest_invalidUser_400Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        MvcResult result = mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "999"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getSaleItemsInterest_invalidRequest_400Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Verify that a 400 response is received in response to the PUT request
        MvcResult result = mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "user.name: Edward"))
                        .andExpect(status().isBadRequest())
                        .andReturn();
    }

    @Test
    void getSaleItemsInterest_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "4"))
                .andExpect(status().isUnauthorized())
                .andReturn();
        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void getSaleItemsInterest_userCannotUpdateInterest_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);

        // Verify that a 403 response is received in response to the GET request
        mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "4"))
                        .andExpect(status().isForbidden())
                        .andReturn();

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(4L)));
    }

    @Test
    void getSaleItemsInterest_invalidListing_406Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
            mockMvc.perform(get(String.format("/listings/999/interest"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("userId", "4"))
                            .andExpect(status().isNotAcceptable())
                            .andReturn();
    }

    @Test
    void getSaleItemsInterest_validListing_200Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "4"))
                .andExpect(status().isOk())
                .andReturn();

        verify(saleItemRepository, times(1)).findById(saleItem.getId());
    }

    @Test
    void getSaleItemsInterest_validListingAndLiked_returnTrue() throws Exception {
        when(saleItem.getInterestedUsers()).thenReturn(Set.of(user));
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        MvcResult result = mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "4"))
                .andExpect(status().isOk())
                .andReturn();

        verify(saleItemRepository, times(1)).findById(saleItem.getId());

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONObject expected = new JSONObject();
        expected.appendField("isInterested", true);
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsInterest_validListingAndNotLiked_returnsFalse() throws Exception {
        when(saleItem.getInterestedUsers()).thenReturn(Set.of());
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        MvcResult result = mockMvc.perform(get(String.format("/listings/%s/interest", saleItem.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "4"))
                        .andExpect(status().isOk())
                        .andReturn();

        verify(saleItemRepository, times(1)).findById(saleItem.getId());

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONObject expected = new JSONObject();
        expected.appendField("isInterested", false);
        assertEquals(expected, response);
    }


    @Test
    void purchaseSaleItem_invalidRequestBodyFormat_400Response() throws Exception {
        JSONObject invalidBody = new JSONObject();
        invalidBody.put("id", 33);

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody.toString()))
                .andExpect(status().isBadRequest());

        // Sale item should not be deleted if request is not successful
        verify(saleItemRepository, times(0)).delete(any());

        // Record of purchase should not be created if request is not successful
        verify(boughtSaleItemRepository, times(0)).save(any());

        // Inventory item should not be updated if request is not successful
        verify(inventoryItemRepository, times(0)).save(any());
    }

    @Test
    void purchaseSaleItem_invalidAuthenticationToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isUnauthorized());

        // Sale item should not be deleted if request is not successful
        verify(saleItemRepository, times(0)).delete(any());

        // Record of purchase should not be created if request is not successful
        verify(boughtSaleItemRepository, times(0)).save(any());

        // Inventory item should not be updated if request is not successful
        verify(inventoryItemRepository, times(0)).save(any());
    }

    @Test
    void purchaseSaleItem_userDoesNotHavePermissionForPurchase_403Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when a user tries to purchase a sale item on behalf of another user
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isForbidden());

        // Sale item should not be deleted if request is not successful
        verify(saleItemRepository, times(0)).delete(any());

        // Record of purchase should not be created if request is not successful
        verify(boughtSaleItemRepository, times(0)).save(any());

        // Inventory item should not be updated if request is not successful
        verify(inventoryItemRepository, times(0)).save(any());
    }

    @Test
    void purchaseSaleItem_saleItemDoesNotExist_406Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        when(saleItemRepository.findById(saleItem.getId())).thenReturn(Optional.empty());

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isNotAcceptable());

        // Sale item should not be deleted if request is not successful
        verify(saleItemRepository, times(0)).delete(any());

        // Record of purchase should not be created if request is not successful
        verify(boughtSaleItemRepository, times(0)).save(any());

        // Inventory item should not be updated if request is not successful
        verify(inventoryItemRepository, times(0)).save(any());
    }

    @Test
    void purchaseSaleItem_userDoesNotExist_406Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        when(userRepository.findById(user.getUserID())).thenReturn(Optional.empty());

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isNotAcceptable());

        // Sale item should not be deleted if request is not successful
        verify(saleItemRepository, times(0)).delete(any());

        // Record of purchase should not be created if request is not successful
        verify(boughtSaleItemRepository, times(0)).save(any());

        // Inventory item should not be updated if request is not successful
        verify(inventoryItemRepository, times(0)).save(any());
    }

    @Test
    void purchaseSaleItem_validRequest_200Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        // Sale item should be deleted if request is successful
        verify(saleItemRepository, times(1)).delete(saleItem);
    }

    @Test
    void purchaseSaleItem_validRequest_purchaseRecordCreated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        var product = Mockito.mock(Product.class);
        when(saleItem.getProduct()).thenReturn(product);

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        // Record of purchase should be created if request is successful
        var boughtSaleItemCaptor = ArgumentCaptor.forClass(BoughtSaleItem.class);
        verify(boughtSaleItemRepository, times(1)).save(boughtSaleItemCaptor.capture());
        assertEquals(user, boughtSaleItemCaptor.getValue().getBuyer());
        assertEquals(product, boughtSaleItemCaptor.getValue().getProduct());
    }

    @Test
    void purchaseSaleItem_validRequest_invItemUpdated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        when(saleItem.getQuantity()).thenReturn(50);
        when(inventoryItem.getQuantity()).thenReturn(120);

        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        // Inventory item should be updated if request is successful
        verify(inventoryItem, times(1)).sellQuantity(50);
        verify(inventoryItemRepository, times(1)).save(inventoryItem);
    }

    @Test
    void purchaseSaleItem_allInventoryItemsSold_inventoryItemDeleted() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        when(inventoryItem.getQuantity()).thenReturn(50);
        doAnswer(invocation -> {
            when(inventoryItem.getQuantity()).thenReturn(0); // Once sold, then set quantity to 0
            return null;
        }).when(inventoryItem).sellQuantity(any());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        verify(inventoryItemRepository, times(0)).save(any());
        verify(inventoryItemRepository, times(1)).delete(inventoryItem);
    }


    @Test
    void purchaseSaleItem_validRequest_purchaseEventCreated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        var product = Mockito.mock(Product.class);
        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        var purchasedEventArgumentCaptor = ArgumentCaptor.forClass(PurchasedEvent.class);
        verify(eventRepository, times(1)).save(purchasedEventArgumentCaptor.capture());
        assertEquals(user, purchasedEventArgumentCaptor.getValue().getBoughtSaleItem().getBuyer());
    }

    @Test
    void purchaseSaleItem_userLikedSaleItemBoughtByAnotherUser_eventCreatedForTheInterestedUser() throws Exception {
        when(saleItem.getInterestedUsers()).thenReturn(Set.of(interestedUser1));
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        verify(eventRepository, times(2)).save(eventCaptor.capture());
        List<Event> allSavedEvents = eventCaptor.getAllValues();
        assertEquals(allSavedEvents.get(0).getClass(), InterestPurchasedEvent.class);
        assertEquals(allSavedEvents.get(1).getClass(), PurchasedEvent.class);
    }

    @Test
    void purchaseSaleItem_noUserLikedSaleItemBoughtByAnotherUser_noEventsCreated() throws Exception {
        when(saleItem.getInterestedUsers()).thenReturn(Set.of());
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        verify(eventRepository, times(1)).save(eventCaptor.capture());
        List<Event> allSavedEvents = eventCaptor.getAllValues();
        assertEquals(allSavedEvents.get(0).getClass(), PurchasedEvent.class);
    }

    @Test
    void purchaseSaleItem_multipleUsersLikedSaleItemBoughtByAnotherUser_eventsCreatedForTheInterestedUsers() throws Exception {
        when(saleItem.getInterestedUsers()).thenReturn(Set.of(interestedUser1, interestedUser2, interestedUser3));
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        JSONObject validBody = new JSONObject();
        validBody.put("purchaserId", user.getUserID());

        mockMvc.perform(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody.toString()))
                .andExpect(status().isOk());

        verify(eventRepository, times(4)).save(eventCaptor.capture());
        List<Event> allSavedEvents = eventCaptor.getAllValues();
        assertEquals(allSavedEvents.get(0).getClass(), InterestPurchasedEvent.class);
        assertEquals(allSavedEvents.get(1).getClass(), InterestPurchasedEvent.class);
        assertEquals(allSavedEvents.get(2).getClass(), InterestPurchasedEvent.class);
        assertEquals(allSavedEvents.get(3).getClass(), PurchasedEvent.class);
    }

    @Test
    void generateReportForBusiness_notLoggedIn_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/reports"))
                .andExpect(status().isUnauthorized());

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verifyNoInteractions(reportService);
    }

    @Test
    void generateReportForBusiness_invalidBusiness_406Response() throws Exception {
        // Verify that a 406 response is received in response to the GET request
        mockMvc.perform(get("/businesses/9999/reports"))
                .andExpect(status().isNotAcceptable());

        verify(businessRepository, times(1)).getBusinessById(9999L);

        verifyNoInteractions(reportService);
    }

    @Test
    void generateReportForBusiness_doesNotHavePermission_403Response() throws Exception {
        doThrow(new InsufficientPermissionResponseException("foo")).when(business).checkSessionPermissions(any());

        // Verify that a 403 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/reports"))
                .andExpect(status().isForbidden());

        verify(businessRepository, times(1)).getBusinessById(1L);
        verify(business, times(1)).checkSessionPermissions(any());

        verifyNoInteractions(reportService);
    }

    @ParameterizedTest
    @CsvSource({
            "2021-aa-01,2021-11-01,none",
            "2020-01-01,2021-aa-01,none",
            "2021-01-01,2021-11-01,nan",
            "2021-10-01,2021-05-01,none", // End date before start date
    })
    void generateReportForBusiness_invalidQueryParams_400Response(String startDate, String endDate, String granularity) throws Exception {
        // Verify that a 400 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/reports")
                .param("startDate", startDate)
                .param("endDate", endDate)
                .param("granularity", granularity))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void generateReportForBusiness_validParameters_200ResponseAndReportReturned() throws Exception {
        var expected = List.of(
                new BoughtSaleItemRecord(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10"), 10, 1, 5, 3, BigDecimal.TEN, 1.0, 0.5)
        );
        when(reportService.generateReport(any(), any(), any(), any())).thenReturn(expected);


        var result = mockMvc.perform(get("/businesses/1/reports")
                .param("startDate", "2021-01-01")
                .param("endDate", "2021-01-10")
                .param("granularity", "daily"))
                .andExpect(status().isOk())
                .andReturn();

        List<BoughtSaleItemRecord> records = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(expected, records);

        verify(reportService, times(1)).generateReport(business, LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10"), ReportGranularity.DAILY);
    }

    @Test
    void generateReportForBusiness_noStartDate_200ResponseAndUsedBusinessCreationDate() throws Exception {
        var expected = List.of(
                new BoughtSaleItemRecord(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10"), 10, 1, 5, 3, BigDecimal.TEN, 1.0, 0.5)
        );
        when(reportService.generateReport(any(), any(), any(), any())).thenReturn(expected);


        var result = mockMvc.perform(get("/businesses/1/reports")
                .param("endDate", "2021-10-10")
                .param("granularity", "monthly"))
                .andExpect(status().isOk())
                .andReturn();

        List<BoughtSaleItemRecord> records = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(expected, records);

        verify(reportService, times(1)).generateReport(business, LocalDate.parse("2021-09-08"), LocalDate.parse("2021-10-10"), ReportGranularity.MONTHLY);
    }

    @Test
    void generateReportForBusiness_noEndDate_200ResponseAndUsedToday() throws Exception {
        var expected = List.of(
                new BoughtSaleItemRecord(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-10"), 10, 1, 5, 3, BigDecimal.TEN, 1.0, 0.5)
        );
        when(reportService.generateReport(any(), any(), any(), any())).thenReturn(expected);


        var result = mockMvc.perform(get("/businesses/1/reports")
                .param("startDate", "2021-01-01")
                .param("granularity", "yearly"))
                .andExpect(status().isOk())
                .andReturn();

        var a = result.getResponse().getContentAsString();
        System.out.println(a);

        List<BoughtSaleItemRecord> records = objectMapper.readValue(a, new TypeReference<>() {});
        assertEquals(expected, records);

        verify(reportService, times(1)).generateReport(business, LocalDate.parse("2021-01-01"), LocalDate.now(), ReportGranularity.YEARLY);
    }
}
