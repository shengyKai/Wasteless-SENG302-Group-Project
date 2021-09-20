package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.dto.KeywordDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.CreateKeywordEventRepository;
import org.seng302.leftovers.service.KeywordService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class KeywordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private KeywordService keywordService;

    @Mock
    private CreateKeywordEventRepository createKeywordEventRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private KeywordCreatedEvent mockEvent;

    @Mock
    private User mockUser;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;
    private final HashMap<String, Object> sessionAttributes = new HashMap<>();

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        when(keywordRepository.findByName(any())).thenReturn(Optional.empty());
        when(createKeywordEventRepository.getByNewKeyword(any())).thenReturn(Optional.of(mockEvent));

        sessionAttributes.put("accountId", 12L);

        var keywordController = new KeywordController(keywordRepository, keywordService, createKeywordEventRepository, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(keywordController).build();
    }


    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    @Test
    void searchKeywords_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/keywords/search"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void searchKeywords_noQueryPresent_returnsAllKeywordList() throws Exception {
        List<Keyword> keywords = new ArrayList<>();
        for (String keywordName : List.of("Keyword One", "Keyword Two", "Keyword Three")) {
            Keyword mockKeyword = mock(Keyword.class);

            when(mockKeyword.getID()).thenReturn(1L);
            when(mockKeyword.getCreated()).thenReturn(Instant.now());
            when(mockKeyword.getName()).thenReturn(keywordName);
            keywords.add(mockKeyword);
        }

        when(keywordRepository.findByOrderByNameAsc()).thenReturn(keywords);

        MvcResult result = mockMvc.perform(get("/keywords/search"))
                .andExpect(status().isOk())
                .andReturn();

        verify(keywordRepository).findByOrderByNameAsc();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();

        expected.addAll(keywords.stream().map(keyword -> objectMapper.convertValue(new KeywordDTO(keyword), JSONObject.class)).collect(Collectors.toList()));

        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expected)),
                objectMapper.readTree(objectMapper.writeValueAsString(response)));
    }

    @Test
    void searchKeywords_QueryEmpty_returnsAllKeywordList() throws Exception {
        List<Keyword> keywords = new ArrayList<>();
        for (String keywordName : List.of("Keyword One", "Keyword Two", "Keyword Three")) {
            Keyword mockKeyword = mock(Keyword.class);

            when(mockKeyword.getID()).thenReturn(1L);
            when(mockKeyword.getCreated()).thenReturn(Instant.now());
            when(mockKeyword.getName()).thenReturn(keywordName);
            keywords.add(mockKeyword);
        }

        when(keywordRepository.findByOrderByNameAsc()).thenReturn(keywords);

        MvcResult result = mockMvc.perform(get("/keywords/search")
                .param("searchQuery", ""))
                .andExpect(status().isOk())
                .andReturn();

        verify(keywordRepository).findByOrderByNameAsc();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();

        expected.addAll(keywords.stream().map(keyword -> objectMapper.convertValue(new KeywordDTO(keyword), JSONObject.class)).collect(Collectors.toList()));

        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expected)),
                objectMapper.readTree(objectMapper.writeValueAsString(response)));
    }

    @Test
    void searchKeywords_withSearchTerm_doesPredicateSearch() throws Exception {
        MvcResult result = mockMvc.perform(get("/keywords/search")
                .param("searchQuery", "One"))
                .andExpect(status().isOk())
                .andReturn();

        verify(keywordRepository).findAll(any(Specification.class)); // couldn't find a way to mock the abstract method
    }

    @Test
    void deleteKeyword_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        // Verify that a 401 response is received in response to the DELETE request
        mockMvc.perform(delete("/keywords/1"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(keywordRepository, times(0)).delete(any()); // Nothing is deleted
    }

    @Test
    void deleteKeyword_notAdmin_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(false);

        // Verify that a 403 response is received in response to the DELETE request
        mockMvc.perform(delete("/keywords/1"))
                .andExpect(status().isForbidden())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionIsAdmin(any()));
        verify(keywordRepository, times(0)).delete(any()); // Nothing is deleted
    }

    @Test
    void deleteKeyword_keywordNotFound_406Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);

        when(keywordRepository.findById(99L)).thenReturn(Optional.empty());

        // Verify that a 406 response is received in response to the DELETE request
        mockMvc.perform(delete("/keywords/99"))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionIsAdmin(any()));
        verify(keywordRepository, times(1)).findById(99L);
        verify(keywordRepository, times(0)).delete(any()); // Nothing is deleted
    }

    @Test
    void deleteKeyword_validRequest_200ResponseAndDeleted() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);

        Keyword mockKeyword = mock(Keyword.class);
        when(keywordRepository.findById(99L)).thenReturn(Optional.of(mockKeyword));

        // Verify that a 200 response is received in response to the DELETE request
        mockMvc.perform(delete("/keywords/99"))
                .andExpect(status().isOk())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionIsAdmin(any()));
        verify(keywordRepository, times(1)).findById(99L);
        verify(keywordRepository, times(1)).delete(mockKeyword); // Keyword is deleted
        verify(createKeywordEventRepository, times(1)).getByNewKeyword(mockKeyword);
        verify(createKeywordEventRepository, times(1)).delete(mockEvent);   // Event is deleted
    }

    @Test
    void addKeyword_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        JSONObject json = new JSONObject();
        json.put("name", "Dance");

        // Verify that a 401 response is received in response to the POST request
        mockMvc.perform(post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addKeyword_keywordAlreadyExists_400Response() throws Exception {
        when(keywordRepository.findByName("Dance")).thenReturn(Optional.of(new Keyword("Dance")));

        JSONObject json = new JSONObject();
        json.put("name", "Dance");

        mockMvc.perform(post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(keywordRepository, times(1)).findByName("Dance");
        verify(keywordRepository, times(0)).save(any());
    }

    @Test
    void addKeyword_invalidKeyword_400Response() throws Exception {
        try (MockedConstruction<Keyword> mocked = Mockito.mockConstruction(Keyword.class, (mock, context) -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        })) {
            JSONObject json = new JSONObject();
            json.put("name", "Dance");

            mockMvc.perform(post("/keywords")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.toJSONString()))
                    .andExpect(status().isBadRequest())
                    .andReturn();
        }
        verify(keywordRepository, times(0)).save(any());
    }

    @Test
    void addKeyword_validRequest_201Response() throws Exception {
        when(keywordRepository.save(any())).thenAnswer(keyword -> keyword.getArgument(0));
        when(userRepository.findById(12L)).thenReturn(Optional.of(mockUser));

        JSONObject json = new JSONObject();
        json.put("name", "Dance");

        mockMvc.perform(MockMvcRequestBuilders.post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString())
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isCreated())
                .andReturn();

        verify(keywordRepository, times(1)).save(any());
        verify(keywordService, times(1)).sendNewKeywordEvent(any(), userArgumentCaptor.capture());
        assertEquals(mockUser, userArgumentCaptor.getValue());
    }
}
