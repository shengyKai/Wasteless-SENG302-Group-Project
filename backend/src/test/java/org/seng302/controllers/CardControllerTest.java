package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private MarketplaceCard mockCard;
    @Mock
    private User mockUser;
    @Mock
    private Keyword mockKeyword1;
    @Mock
    private Keyword mockKeyword2;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;
    private JSONObject createCardJson;
    private final long userId = 17L;
    private final long cardId = 32L;
    private final long keywordId1 = 25L;
    private final long keywordId2 = 71L;

    @BeforeEach
    private void setUp() {
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

        // Set up entitis to return set id when getter called
        when(mockCard.getID()).thenReturn(cardId);
        when(mockUser.getUserID()).thenReturn(userId);

        // Tell MockMvc to use controller with mocked repositories for tests
        CardController cardController = new CardController(marketplaceCardRepository, keywordRepository, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();

        constructValidCreateCardJson();
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

}