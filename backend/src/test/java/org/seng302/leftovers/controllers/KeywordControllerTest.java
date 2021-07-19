package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.AccessTokenException;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
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

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        when(keywordRepository.findByName(any())).thenReturn(Optional.empty());

        var keywordController = new KeywordController(keywordRepository);
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
                .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/keywords/search"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void searchKeywords_withAuthentication_returnsKeywordList() throws Exception {
        List<Keyword> keywords = new ArrayList<>();
        for (String keywordName : List.of("Keyword One", "Keyword Two", "Keyword Three")) {
            Keyword mockKeyword = mock(Keyword.class);

            JSONObject mockResponse = new JSONObject();
            mockResponse.put("name", keywordName);
            when(mockKeyword.constructJSONObject()).thenReturn(mockResponse);

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

        expected.addAll(keywords.stream().map(Keyword::constructJSONObject).collect(Collectors.toList()));

        assertEquals(expected, response);
    }

    @Test
    void addKeyword_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

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
        when(keywordRepository.findByName("Dance")).thenReturn(Optional.of(mock(Keyword.class)));

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

        JSONObject json = new JSONObject();
        json.put("name", "Dance");

        mockMvc.perform(post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(status().isCreated())
                .andReturn();

        verify(keywordRepository, times(1)).save(any());
    }
}
