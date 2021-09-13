package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.exceptions.AccessTokenException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
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
    private Business business;
    @Mock
    private User user;
    @Mock
    private InventoryItem inventoryItem;
    @Mock
    private SaleItem saleItem;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private SaleController saleController;

    @Captor
    ArgumentCaptor<Specification<SaleItem>> specificationArgumentCaptor;
    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;


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
        when(saleItemRepository.findById(not(eq(3L)))).thenReturn(Optional.empty());
        when(saleItemRepository.findById(3L)).thenReturn(Optional.of(saleItem));

        when(saleItem.getId()).thenReturn(3L);

        // Setup mock user
        when(user.getUserID()).thenReturn(4L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.findById(not(eq(4L)))).thenReturn(Optional.empty());

        saleController = spy(new SaleController(userRepository, businessRepository, saleItemRepository, inventoryItemRepository, interestEventRepository));
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
        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "price").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_noSortOrder_usesCreatedSortOrder() throws Exception {
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/businesses/1/listings"))
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

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
            SaleItem saleItem = mock(SaleItem.class);
            when(saleItem.getId()).thenReturn(i);
            var json = new JSONObject();
            json.put("id", i);
            when(saleItem.constructJSONObject()).thenReturn(json);
            mockItems.add(saleItem);
        }
        // Ensure determinism
        Collections.shuffle(mockItems, new Random(7));
        return mockItems;
    }

    @Test
    void getSaleItemsForBusiness_noReverse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<SaleItem>(items));
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_reverseFalse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<SaleItem>(items));
        saleItemRepository.saveAll(items);

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_reverseTrue_itemsDescending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<SaleItem>(items));
        saleItemRepository.saveAll(items);


        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "true"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.DESC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_resultsPerPageSet_firstPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<SaleItem>(items));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(null, 4, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(saleItemRepository, times(1)).findAll(specificationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());
        assertTrue(new ReflectionEquals(expectedSpecification).matches(specificationArgumentCaptor.getValue()));
        assertTrue(new ReflectionEquals(expectedPageRequest).matches(pageRequestArgumentCaptor.getValue()));
    }

    @Test
    void getSaleItemsForBusiness_secondPageRequested_secondPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<SaleItem>(items));
        saleItemRepository.saveAll(items);


        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4")
                .param("page","2"))
                .andExpect(status().isOk())
                .andReturn();

        Specification<SaleItem> expectedSpecification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
        PageRequest expectedPageRequest = SearchHelper.getPageRequest(2, 4, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

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
                .thenThrow(new AccessTokenException());

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
}
