package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.AccessTokenException;
import org.seng302.leftovers.persistence.ExpiryEventRepository;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private MarketplaceCardRepository marketplaceCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExpiryEventRepository expiryEventRepository;
    @Mock
    private MarketplaceCard mockCard;
    @Mock
    private ExpiryEvent mockEvent;
    @Mock
    private User mockUser;
    @Mock
    private Page<MarketplaceCard> mockPage;
    @Mock
    private Keyword mockKeyword1;
    @Mock
    private Keyword mockKeyword2;
    @Mock
    private HttpServletRequest request;

    private User testUser;
    private User testUser1;
    private CardController cardController;
    private List<MarketplaceCard> cards = new ArrayList<>();

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;
    private JSONObject createCardJson;
    private final long userId = 17L;
    private final long cardId = 32L;
    private final long keywordId1 = 25L;
    private final long keywordId2 = 71L;

    @BeforeEach
    private void setUp() throws Exception {
        testUser = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Cory")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testUser1 = new User.Builder()
                .withFirstName("Bobby")
                .withMiddleName("Percy")
                .withLastName("David")
                .withNickName("Ando")
                .withEmail("456andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
                
        MockitoAnnotations.openMocks(this);

        // Set up authentication manager respond as if user has correct permissions to create card
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Set up repositories that will be queried when creating card to return mocks
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(keywordRepository.findById(keywordId1)).thenReturn(Optional.of(mockKeyword1));
        when(keywordRepository.findById(keywordId2)).thenReturn(Optional.of(mockKeyword2));

        when(marketplaceCardRepository.save(any())).thenReturn(mockCard);
        when(marketplaceCardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(marketplaceCardRepository.findById(not(eq(1L)))).thenReturn(Optional.empty());
        when(marketplaceCardRepository.getCard(any())).thenCallRealMethod();

        when(marketplaceCardRepository.getAllBySection(any(MarketplaceCard.Section.class), any(PageRequest.class))).thenReturn(mockPage);
        when(mockPage.getTotalElements()).thenReturn(30L);
        when(mockPage.iterator()).thenReturn(List.of(mockCard).iterator());

        // Set up entities to return set id when getter called
        when(mockCard.getID()).thenReturn(cardId);
        when(mockCard.getCreator()).thenReturn(mockUser);
        when(mockUser.getUserID()).thenReturn(userId);

        // Tell MockMvc to use controller with mocked repositories for tests
        cardController = new CardController(marketplaceCardRepository, keywordRepository, userRepository, expiryEventRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();

        constructValidCreateCardJson();

        addSeveralMarketplaceCards(cards);
        when(marketplaceCardRepository.getAllBySection(any())).thenReturn(cards);
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    /**
     * Construct a JSON object suitable for the body of a create card request
     */
    private void constructValidCreateCardJson() {
        createCardJson = new JSONObject();
        createCardJson.appendField("title", "Free feijoas");
        createCardJson.appendField("creatorId", (int) userId);
        createCardJson.appendField("section", "ForSale");
        createCardJson.appendField("keywordIds", new int[0]);
    }

    @Test
    void createCard_invalidAuthToken_cannotCreateCard() throws Exception {
       authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());
       mockMvc.perform(post("/cards")
               .contentType(MediaType.APPLICATION_JSON)
               .content(createCardJson.toString()))
               .andExpect(status().isUnauthorized());
       verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_noPermissionForCreatorAccount_cannotCreateCard() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);
        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isForbidden());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_requiredFieldMissing_cannotCreateCard() throws Exception {
        createCardJson.remove("title");
        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("Card title must be provided", result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_invalidField_cannotCreateCard() throws Exception {
        createCardJson.remove("title");
        createCardJson.appendField("title", "a".repeat(51));
        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("Card title must be between 1-50 characters long", result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_onlyRequiredFieldsPresent_successfulResponse() throws Exception {
        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject expectedJson = new JSONObject();
        expectedJson.appendField("cardId", (int) cardId);
        assertEquals(expectedJson.toJSONString(), result.getResponse().getContentAsString());
    }

    @Test
    void createCard_onlyRequiredFieldsPresent_cardSaved() throws Exception {
        Instant before = Instant.now();

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated());

        Instant after = Instant.now();

        ArgumentCaptor<MarketplaceCard> captor = ArgumentCaptor.forClass(MarketplaceCard.class);
        verify(marketplaceCardRepository).save(captor.capture());
        MarketplaceCard savedCard = captor.getValue();

        // Verify all attributes of the card saved to the repository have the expected value
        assertEquals(createCardJson.getAsString("title"), savedCard.getTitle());
        assertEquals(createCardJson.getAsString("section"), savedCard.getSection().getName());
        assertEquals(Long.parseLong(createCardJson.getAsString("creatorId")), savedCard.getCreator().getUserID());
        Keyword[] expectedKeywords = new Keyword[0];
        List<Keyword> actualKeywords = (List<Keyword>) ReflectionTestUtils.getField(savedCard, "keywords");
        assertNotNull(actualKeywords);
        assertArrayEquals(expectedKeywords, actualKeywords.toArray());
        assertTrue(before.isBefore(savedCard.getCreated()));
        assertTrue(after.isAfter(savedCard.getCreated()));
    }

    @Test
    void createCard_optionalFieldsPresent_successfulResponse() throws Exception {
        createCardJson.appendField("description", "This is the description");
        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject expectedJson = new JSONObject();
        expectedJson.appendField("cardId", (int) cardId);
        assertEquals(expectedJson.toJSONString(), result.getResponse().getContentAsString());
    }

    @Test
    void createCard_optionalFieldsPresent_cardCreated() throws Exception {
        Instant before = Instant.now();

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated());

        Instant after = Instant.now();

        ArgumentCaptor<MarketplaceCard> captor = ArgumentCaptor.forClass(MarketplaceCard.class);
        verify(marketplaceCardRepository).save(captor.capture());
        MarketplaceCard savedCard = captor.getValue();

        // Verify all attributes of the card saved to the repository have the expected value
        assertEquals(createCardJson.getAsString("title"), savedCard.getTitle());
        assertEquals(createCardJson.getAsString("section"), savedCard.getSection().getName());
        assertEquals(Long.parseLong(createCardJson.getAsString("creatorId")), savedCard.getCreator().getUserID());
        Keyword[] expectedKeywords = new Keyword[0];
        List<Keyword> actualKeywords = (List<Keyword>) ReflectionTestUtils.getField(savedCard, "keywords");
        assertNotNull(actualKeywords);
        assertArrayEquals(expectedKeywords, actualKeywords.toArray());
        assertTrue(before.isBefore(savedCard.getCreated()));
        assertTrue(after.isAfter(savedCard.getCreated()));
        assertEquals(createCardJson.getAsString("description"), savedCard.getDescription());
    }

    @Test
    void createCard_multipleKeywordIds_successfulResponse() throws Exception {

        createCardJson.remove("keywordIds");
        int[] keywordIds = new int[] {(int) keywordId1, (int) keywordId2};
        createCardJson.appendField("keywordIds", keywordIds);
        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject expectedJson = new JSONObject();
        expectedJson.appendField("cardId", (int) cardId);
        assertEquals(expectedJson.toJSONString(), result.getResponse().getContentAsString());
    }

    @Test
    void createCard_multipleKeywordIds_cardCreated() throws Exception {
        Instant before = Instant.now();

        createCardJson.remove("keywordIds");
        int[] keywordIds = new int[] {(int) keywordId1, (int) keywordId2};
        createCardJson.appendField("keywordIds", keywordIds);
        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        Instant after = Instant.now();

        ArgumentCaptor<MarketplaceCard> captor = ArgumentCaptor.forClass(MarketplaceCard.class);
        verify(marketplaceCardRepository).save(captor.capture());
        MarketplaceCard savedCard = captor.getValue();

        // Verify all attributes of the card saved to the repository have the expected value
        assertEquals(createCardJson.getAsString("title"), savedCard.getTitle());
        assertEquals(createCardJson.getAsString("section"), savedCard.getSection().getName());
        assertEquals(Long.parseLong(createCardJson.getAsString("creatorId")), savedCard.getCreator().getUserID());
        Keyword[] expectedKeywords = new Keyword[] {mockKeyword1, mockKeyword2};
        List<Keyword> actualKeywords = (List<Keyword>) ReflectionTestUtils.getField(savedCard, "keywords");
        assertNotNull(actualKeywords);
        assertArrayEquals(expectedKeywords, actualKeywords.toArray());
        assertTrue(before.isBefore(savedCard.getCreated()));
        assertTrue(after.isAfter(savedCard.getCreated()));
    }

    @Test
    void createCard_creatorIdNotNumber_cardNotCreated() throws Exception {
        createCardJson.remove("creatorId");
        createCardJson.appendField("creatorId", "notValid");

        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("creatorId must be a number", result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_creatorIdNotInRepository_cardNotCreated() throws Exception {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(String.format("User with ID %d does not exist", userId), result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_keywordIdsNotNumberArray_cardNotCreated() throws Exception {
        createCardJson.remove("keywordIds");
        createCardJson.appendField("keywordIds", "notValid");

        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("keywordIds must be an array of numbers", result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_keywordIdNotInRepository_cardNotCreated() throws Exception {
        createCardJson.remove("keywordIds");
        int[] keywordIds = new int[] {(int) keywordId1};
        createCardJson.appendField("keywordIds", keywordIds);
        when(keywordRepository.findById(keywordId1)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(String.format("Keyword with ID %d does not exist", keywordId1), result.getResponse().getErrorMessage());
        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    // GET CARDS TESTS

    @Test
    void getCards_invalidAuthToken_CannotViewCards() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());
        mockMvc.perform(get("/cards")
                .param("section", "Wanted"))
                .andExpect(status().isUnauthorized());
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(MarketplaceCard.Section.class));
    }

    @Test
    void getCards_invalidSection_CannotViewCards() throws Exception {
        mockMvc.perform(get("/cards")
                .param("section", "invalidSectionName"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(MarketplaceCard.Section.class));
    }

    @Test
    void getCards_invalidOrdering_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards")
                    .param("section", "Wanted")
                    .param("orderBy", "invalidOrdering"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    void getCards_noSectionGiven_CannotViewCards() throws Exception {
        mockMvc.perform(get("/cards"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(MarketplaceCard.Section.class));
    }

    @ParameterizedTest
    @EnumSource(MarketplaceCard.Section.class)
    void getCards_validSection_canViewCardsForSection(MarketplaceCard.Section section) throws Exception {
        var result = mockMvc.perform(get("/cards")
                .param("section", section.getName())
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("orderBy", "created")
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();
        var expectedPageRequest = SearchHelper.getPageRequest(6, 8, Sort.by(Sort.Direction.ASC, "created"));
        verify(marketplaceCardRepository).getAllBySection(section, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(mockCard.constructJSONObject());
        assertEquals(expectedResults, responseBody.get("results"));

        assertEquals(2, responseBody.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"created", "title", "closes", "creatorFirstName", "creatorLastName"})
    void getCards_validOrdering_canViewCardsWithOrdering(String ordering) throws Exception {
        var result = mockMvc.perform(get("/cards")
                .param("section", "Wanted")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("orderBy", ordering)
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();
        var expectedPageRequest = SearchHelper.getPageRequest(6, 8, Sort.by(Sort.Direction.ASC, ordering));
        verify(marketplaceCardRepository).getAllBySection(MarketplaceCard.Section.WANTED, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(mockCard.constructJSONObject());
        assertEquals(expectedResults, responseBody.get("results"));

        assertEquals(2, responseBody.size());
    }

    /**
     * Creates several marketplace cards based on a product. These items have
     * differing attributes to identify them.
     * 
     * @throws Exception
     */
    public void addSeveralMarketplaceCards(List<MarketplaceCard> cards) throws Exception {
        cards.add(new MarketplaceCard.Builder().withCreator(testUser).withCloses(Instant.now().plus(1, ChronoUnit.HOURS))
            .withSection("ForSale").withTitle("abcd").build());
        cards.add(new MarketplaceCard.Builder().withCreator(testUser1).withCloses(Instant.now().plus(2, ChronoUnit.HOURS))
            .withSection("ForSale").withTitle("efgh").build());
        cards.add(new MarketplaceCard.Builder().withCreator(testUser).withCloses(Instant.now().plus(3, ChronoUnit.HOURS))
            .withSection("ForSale").withTitle("ijkl").build());
        cards.add(new MarketplaceCard.Builder().withCreator(testUser1).withCloses(Instant.now().plus(4, ChronoUnit.HOURS))
            .withSection("ForSale").withTitle("mnop").build());
    }


    @Test
    void extendCardDisplayPeriod_noAuthToken_401Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());
        mockMvc.perform(put("/cards/1/extenddisplayperiod")).andExpect(status().isUnauthorized());
        verify(mockCard, times(0)).delayCloses();
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void extendCardDisplayPeriod_cardDoesNotExist_404Response() throws Exception {
        mockMvc.perform(put("/cards/2/extenddisplayperiod")).andExpect(status().isNotFound());
        verify(mockCard, times(0)).delayCloses();
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void extendCardDisplayPeriod_doesNotHavePermission_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);
        mockMvc.perform(put("/cards/1/extenddisplayperiod")).andExpect(status().isForbidden());
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(userId)));
        verify(mockCard, times(0)).delayCloses();
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void extendCardDisplayPeriod_cardExistsAndIsAuthorised_200ResponseAndExtended() throws Exception {
        mockMvc.perform(put("/cards/1/extenddisplayperiod")).andExpect(status().isOk());
        verify(mockCard, times(1)).delayCloses();
        verify(marketplaceCardRepository, times(1)).save(mockCard);
    }

    @Test
    void extendCardDisplayPeriod_cardExistsAndFailsToClose_400() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockCard).delayCloses();

        mockMvc.perform(put("/cards/1/extenddisplayperiod")).andExpect(status().isBadRequest());
        verify(mockCard, times(1)).delayCloses();
        verify(marketplaceCardRepository, times(0)).save(mockCard);
    }

    @Test
    void deleteCard_noAuthToken_401Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());
        mockMvc.perform(delete("/cards/1")).andExpect(status().isUnauthorized());
        verify(marketplaceCardRepository, times(0)).delete(any());
    }

    @Test
    void deleteCard_cardDoesNotExist_404Response() throws Exception {
        mockMvc.perform(delete("/cards/2")).andExpect(status().isNotFound());
        verify(marketplaceCardRepository, times(0)).delete(any());
    }

    @Test
    void deleteCard_doesNotHavePermission_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);
        mockMvc.perform(delete("/cards/1")).andExpect(status().isForbidden());
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(userId)));
        verify(marketplaceCardRepository, times(0)).delete(any());
    }

    @Test
    void deleteCard_cardExistsAndIsAuthorised_200ResponseAndDeleted() throws Exception {
        mockMvc.perform(delete("/cards/1")).andExpect(status().isOk());
        verify(marketplaceCardRepository, times(1)).delete(mockCard);
    }

    @Test
    void deleteCard_cardHasAssociatedExpiryEvent_200ResponseAndBothDeleted() throws Exception {
        Optional<ExpiryEvent> optionalExpiryEvent = Optional.of(mockEvent);
        when(expiryEventRepository.getByExpiringCard(mockCard)).thenReturn(optionalExpiryEvent);
        mockMvc.perform(delete("/cards/1")).andExpect(status().isOk());
        verify(expiryEventRepository, times(1)).delete(mockEvent);
        verify(marketplaceCardRepository, times(1)).delete(mockCard);
    }
}