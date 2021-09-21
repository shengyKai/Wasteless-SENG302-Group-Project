package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.seng302.leftovers.dto.card.MarketplaceCardResponseDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.ExpiryEvent;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.SearchMarketplaceCardHelper;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.ExpiryEventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.searchservice.SearchPageConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private ObjectMapper objectMapper;
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
    private Keyword mockKeyword1;
    @Mock
    private Keyword mockKeyword2;
    @Mock
    private Specification<MarketplaceCard> keywordSpec;
    @Mock
    private Specification<MarketplaceCard> sectionSpec;
    @Mock
    private Specification<MarketplaceCard> combinedSpec;

    private Page<MarketplaceCard> expectedPage;

    private User testUser;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private MarketplaceCard testCard1;
    private MarketplaceCard testCard2;
    private MarketplaceCard testCard3;


    private CardController cardController;
    private List<MarketplaceCard> cards = new ArrayList<>();

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;
    private MockedStatic<SearchMarketplaceCardHelper> searchMarketplaceCardHelper;

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
        testUser2 = new User.Builder()
                .withFirstName("Stuart")
                .withMiddleName("Derp")
                .withLastName("Alex")
                .withNickName("Derpy")
                .withEmail("stuart@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Waimairi,Auckland,New Zealand,Auckland,8041"))
                .build();
        testUser3 = new User.Builder()
                .withFirstName("Rick")
                .withMiddleName("Morty")
                .withLastName("Pickle")
                .withNickName("Danger")
                .withEmail("ricknmorty@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Singapore,Singapore,Singapore,Singapore,8041"))
                .build();
        var closes = Instant.now().plus(23, ChronoUnit.HOURS);
        testCard1 = new MarketplaceCard.Builder()
                .withCreator(testUser1)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();
        testCard2 = new MarketplaceCard.Builder()
                .withCreator(testUser2)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();
        testCard3 = new MarketplaceCard.Builder()
                .withCreator(testUser3)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();
                
        MockitoAnnotations.openMocks(this);

        searchMarketplaceCardHelper = Mockito.mockStatic(SearchMarketplaceCardHelper.class);

        // Set up authentication manager respond as if user has correct permissions to create card
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        // Set up repositories that will be queried when creating card to return mocks
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockKeyword1.getID()).thenReturn(keywordId1);
        when(mockKeyword2.getID()).thenReturn(keywordId2);
        when(keywordRepository.findById(keywordId1)).thenReturn(Optional.of(mockKeyword1));
        when(keywordRepository.findById(keywordId2)).thenReturn(Optional.of(mockKeyword2));
        when(keywordRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Keyword> answer = new ArrayList<>();
            for (Long id : ids) {
                if (id == keywordId1) {
                    answer.add(mockKeyword1);
                } else if (id == keywordId2) {
                    answer.add(mockKeyword2);
                } else {
                    return List.of();
                }
            }
            return answer;
        });

        when(marketplaceCardRepository.save(any())).thenReturn(mockCard);
        when(marketplaceCardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(marketplaceCardRepository.findById(not(eq(1L)))).thenReturn(Optional.empty());
        when(marketplaceCardRepository.getCard(any())).thenCallRealMethod();

        expectedPage = new PageImpl<>(List.of(mockCard), Pageable.unpaged(), 30L);

        when(marketplaceCardRepository.getAllBySection(any(), any())).thenReturn(expectedPage);
        when(marketplaceCardRepository.getAllByCreator(any(), any())).thenReturn(expectedPage);
        when(marketplaceCardRepository.findAll(any(), any(Pageable.class))).thenReturn(expectedPage);

        Location testLocation = new Location.Builder()
                .inCountry("New Zealand")
                .inRegion("Canterbury")
                .inCity("Christchurch")
                .atStreetNumber("12")
                .onStreet("Cool street")
                .withPostCode("1234")
                .atDistrict("District")
                .build();

        // Set up entities to return set id when getter called
        when(mockCard.getID()).thenReturn(cardId);
        when(mockCard.getCreator()).thenReturn(mockUser);
        when(mockUser.getUserID()).thenReturn(userId);
        when(mockUser.getAddress()).thenReturn(testLocation);

        // Set up keywordSpec and sectionSpec so that when they are combined with "and" they return combined spec
        when(keywordSpec.and(sectionSpec)).thenReturn(combinedSpec);
        when(sectionSpec.and(keywordSpec)).thenReturn(combinedSpec);

        // Tell MockMvc to use controller with mocked repositories for tests
        cardController = new CardController(marketplaceCardRepository, keywordRepository, userRepository, expiryEventRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();

        constructValidCreateCardJson();

        addSeveralMarketplaceCards(cards);
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
        searchMarketplaceCardHelper.close();
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

    /**
     * Sets up the marketplace cards for address ordering testing. Actual cards are needed as the address parts itself needs to be tested.
     */
    private void setUpAddressOrderingForGetCards() {
        expectedPage = new PageImpl<>(List.of(testCard1, testCard2, testCard3), Pageable.unpaged(), 3L);
        when(marketplaceCardRepository.getAllBySection(any(MarketplaceCard.Section.class), any(PageRequest.class))).thenReturn(expectedPage);
    }

    @Test
    void createCard_invalidAuthToken_cannotCreateCard() throws Exception {
       authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
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
        assertFalse(before.isAfter(savedCard.getCreated()));
        assertFalse(after.isBefore(savedCard.getCreated()));
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
        assertFalse(before.isAfter(savedCard.getCreated()));
        assertFalse(after.isBefore(savedCard.getCreated()));
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
        assertFalse(before.isAfter(savedCard.getCreated()));
        assertFalse(after.isBefore(savedCard.getCreated()));
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

        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_creatorIdNotInRepository_cardNotCreated() throws Exception {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

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

        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    @Test
    void createCard_keywordIdNotInRepository_cardNotCreated() throws Exception {
        createCardJson.remove("keywordIds");
        int[] keywordIds = new int[] {9999};
        createCardJson.appendField("keywordIds", keywordIds);

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(marketplaceCardRepository, times(0)).save(any(MarketplaceCard.class));
    }

    // MODIFY CARD TESTS

    @Test
    void modifyCard_invalidAuthToken_401Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isUnauthorized());
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_cardNotFound_406Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        mockMvc.perform(put("/cards/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isNotAcceptable());
        verify(marketplaceCardRepository, times(0)).save(any());
    }


    @Test
    void modifyCard_userDoesNotHavePermission_403Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        long userID = mockUser.getUserID();
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(userID))).thenReturn(false);

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isForbidden());
        verify(marketplaceCardRepository, times(0)).save(any());
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(userID)));
    }

    @Test
    void modifyCard_invalidKeyword_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field
        createCardJson.put("keywordIds", new int[]{ 1, 9999 });

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_noSectionProvided_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field
        createCardJson.remove("section");

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_invalidSection_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field
        createCardJson.put("section", "something");

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_invalidTitle_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockCard).setTitle(any());

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_invalidDescription_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockCard).setDescription(any());

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_invalidKeywords_400Response() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockCard).setKeywords(any());

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void modifyCard_validRequest_cardModified() throws Exception {
        createCardJson.remove("creatorId"); // Will not need this field
        List<Keyword> keywords = List.of(mockKeyword1, mockKeyword2);
        createCardJson.put("keywordIds", keywords.stream().map(Keyword::getID).collect(Collectors.toList()));

        mockMvc.perform(put("/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardJson.toString()))
                .andExpect(status().isOk());

        verify(mockCard).setSection(MarketplaceCard.sectionFromString(createCardJson.getAsString("section")));
        verify(mockCard).setTitle(createCardJson.getAsString("title"));
        verify(mockCard).setDescription(createCardJson.getAsString("description"));
        verify(mockCard).setKeywords(keywords);

        verify(marketplaceCardRepository, times(1)).save(mockCard);
    }


    // GET CARDS TESTS

    @Test
    void getCards_invalidAuthToken_CannotViewCards() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(get("/cards")
                .param("section", "Wanted"))
                .andExpect(status().isUnauthorized());
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(), any());
    }

    @Test
    void getCards_invalidSection_CannotViewCards() throws Exception {
        mockMvc.perform(get("/cards")
                .param("section", "invalidSectionName"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(), any());
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
        verify(marketplaceCardRepository, times(0)).getAllBySection(any(), any());
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
        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));
        verify(marketplaceCardRepository).getAllBySection(section, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(mockCard), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedResults)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseBody.get("results"))));

        assertEquals(2, responseBody.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"lastRenewed", "created", "title", "closes", "creatorFirstName", "creatorLastName"})
    void getCards_validOrdering_canViewCardsWithOrdering(String ordering) throws Exception {
        var result = mockMvc.perform(get("/cards")
                .param("section", "Wanted")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("orderBy", ordering)
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();
        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(new Sort.Order(Sort.Direction.ASC, ordering).ignoreCase()));
        verify(marketplaceCardRepository).getAllBySection(MarketplaceCard.Section.WANTED, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(mockCard), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedResults)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseBody.get("results"))));

        assertEquals(2, responseBody.size());
    }

    // SEARCH CARDS TESTS

    @Test
    void searchCards_invalidAuthToken_cannotViewCards() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        var res = mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("keywordIds", "1")
                .param("union", "true"))
                .andExpect(status().isUnauthorized());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @Test
    void searchCards_invalidSection_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("section", "invalidSectionName")
                .param("keywordIds", "1")
                .param("union", "true"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @Test
    void searchCards_invalidOrdering_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("orderBy", "invalidOrdering")
                .param("keywordIds", "1")
                .param("union", "true"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void searchCards_noSectionGiven_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("keywordIds", "1")
                .param("union", "true"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @Test
    void searchCards_noKeywordsGiven_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("union", "true"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @Test
    void searchCards_noUnionGiven_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("keywordIds", "1"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @Test
    void searchCards_invalidKeywordId_cannotViewCards() throws Exception {
        mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("keywordIds", "9999")
                .param("union", "true"))
                .andExpect(status().isBadRequest());
        verify(marketplaceCardRepository, times(0)).findAll(any(), any(Pageable.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"lastRenewed", "created", "title", "closes", "creatorFirstName", "creatorLastName"})
    void searchCards_validOrdering_canViewCardsWithOrdering(String ordering) throws Exception {
        // Set up the specification generators for the expected arguments
        searchMarketplaceCardHelper.when(() -> SearchMarketplaceCardHelper.cardHasKeywords(List.of(mockKeyword1, mockKeyword2), true)).thenReturn(keywordSpec);
        searchMarketplaceCardHelper.when(() -> SearchMarketplaceCardHelper.cardIsInSection(MarketplaceCard.Section.WANTED)).thenReturn(sectionSpec);

        var result = mockMvc.perform(get("/cards/search")
                .param("section", "Wanted")
                .param("keywordIds",  String.valueOf(keywordId1), String.valueOf(keywordId2))
                .param("union", "true")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("orderBy", ordering)
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();

        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(new Sort.Order(Sort.Direction.ASC, ordering).ignoreCase()));

        verify(marketplaceCardRepository).findAll(combinedSpec, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(mockCard), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedResults)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseBody.get("results"))));

        assertEquals(2, responseBody.size());
    }

    @ParameterizedTest
    @EnumSource(MarketplaceCard.Section.class)
    void searchCards_validSection_canViewCardsForSection(MarketplaceCard.Section section) throws Exception {
        // Set up the specification generators for the expected arguments
        searchMarketplaceCardHelper.when(() -> SearchMarketplaceCardHelper.cardHasKeywords(List.of(mockKeyword1), false)).thenReturn(keywordSpec);
        searchMarketplaceCardHelper.when(() -> SearchMarketplaceCardHelper.cardIsInSection(section)).thenReturn(sectionSpec);

        var result = mockMvc.perform(get("/cards/search")
                .param("section", section.getName())
                .param("keywordIds",  String.valueOf(keywordId1))
                .param("union", "false")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("orderBy", "created")
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();

        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(new Sort.Order(Sort.Direction.ASC, "created").ignoreCase()));

        verify(marketplaceCardRepository).findAll(combinedSpec, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(mockCard), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedResults)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseBody.get("results"))));

        assertEquals(2, responseBody.size());
    }

    /**
     * Creates several marketplace cards based on a product. These items have
     * differing attributes to identify them.
     * 
     * @throws Exception Exception
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
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(put("/cards/1/extenddisplayperiod")).andExpect(status().isUnauthorized());
        verify(mockCard, times(0)).delayCloses();
        verify(marketplaceCardRepository, times(0)).save(any());
    }

    @Test
    void extendCardDisplayPeriod_cardDoesNotExist_406Response() throws Exception {
        mockMvc.perform(put("/cards/2/extenddisplayperiod")).andExpect(status().isNotAcceptable());
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
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(delete("/cards/1")).andExpect(status().isUnauthorized());
        verify(marketplaceCardRepository, times(0)).delete(any());
    }

    @Test
    void deleteCard_cardDoesNotExist_406Response() throws Exception {
        mockMvc.perform(delete("/cards/2")).andExpect(status().isNotAcceptable());
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

    @Test
    void getCardsForUser_noAuthToken_401Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(get("/users/1/cards")).andExpect(status().isUnauthorized());
        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(marketplaceCardRepository, times(0)).getAllByCreator(any());
    }

    @Test
    void getCardsForUser_userDoesNotExist_406Response() throws Exception {
        mockMvc.perform(get("/users/9999/cards")).andExpect(status().isNotAcceptable());
        verify(marketplaceCardRepository, times(0)).getAllByCreator(any());
    }

    @Test
    void getCardsForUser_validUser_usersCardsReturned() throws Exception {
        var result = mockMvc.perform(get("/users/" + userId + "/cards")
                .param("resultsPerPage", "8")
                .param("page", "6"))
                .andExpect(status().isOk())
                .andReturn();
        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(new Sort.Order(Sort.Direction.DESC, "lastRenewed")));
        verify(marketplaceCardRepository).getAllByCreator(mockUser, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(30, responseBody.get("count"));

        var expectedResults = new JSONArray();
        expectedResults.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(mockCard), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedResults)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseBody.get("results"))));

        assertEquals(2, responseBody.size());
    }

    
    @Test
    void getCards_orderByAddress_cardsReturnedWithValidAddressOrdering() throws Exception {
        setUpAddressOrderingForGetCards();
        var result = mockMvc.perform(get("/cards")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("section", "Wanted")
                .param("orderBy", "location"))
                .andExpect(status().isOk())
                .andReturn();
        
        //verify the arguments for the method call are the same
        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(List.of(new Sort.Order(Sort.Direction.ASC, "creator.address.country").ignoreCase(), new Sort.Order(Sort.Direction.ASC, "creator.address.city").ignoreCase())));
        verify(marketplaceCardRepository).getAllBySection(MarketplaceCard.Section.WANTED, expectedPageRequest);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertEquals(3, responseBody.get("count"));
        JSONArray responseArray = (JSONArray) responseBody.get("results");
        JSONArray expectedArray = new JSONArray();
        expectedArray.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(testCard1), JSONObject.class));
        expectedArray.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(testCard2), JSONObject.class));
        expectedArray.add(objectMapper.convertValue(new MarketplaceCardResponseDTO(testCard3), JSONObject.class));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedArray)) ,
                objectMapper.readTree(objectMapper.writeValueAsString(responseArray)));
    }

    @Test
    void getCards_orderByAddressReverse_argumentsMatchForSortByCall() throws Exception {
        var result = mockMvc.perform(get("/cards")
                .param("resultsPerPage", "8")
                .param("page", "6")
                .param("section", "Wanted")
                .param("orderBy", "location")
                .param("reverse", "true"))
                .andExpect(status().isOk())
                .andReturn();
        
        //verify the arguments for the method call are the same
        var expectedPageRequest = SearchPageConstructor.getPageRequest(6, 8, Sort.by(List.of(new Sort.Order(Sort.Direction.DESC, "creator.address.country").ignoreCase(), new Sort.Order(Sort.Direction.DESC, "creator.address.city").ignoreCase())));
        verify(marketplaceCardRepository).getAllBySection(MarketplaceCard.Section.WANTED, expectedPageRequest); 
    }
}