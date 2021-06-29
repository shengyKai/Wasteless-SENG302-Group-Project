package org.seng302.controllers;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.entities.Business;
import org.seng302.entities.Event;
import org.seng302.entities.MessageEvent;
import org.seng302.entities.User;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.UserRepository;
import org.seng302.service.EventService;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventControllerTest {
    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User mockUser;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private EventController eventController;

    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Make sure that user is admin by default
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);

        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userRepository.findById(eq(7L))).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(not(eq(7L)))).thenReturn(Optional.empty());


        eventController = new EventController(userRepository, eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @AfterEach
    void tearDown() {
        authenticationTokenManager.close();
    }


    @Test
    void postDemoEvent_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        var json = new JSONObject();
        json.put("message", "this that");
        mockMvc.perform(post("/events/globalmessage").contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void postDemoEvent_notAdmin_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(false);

        var json = new JSONObject();
        json.put("message", "this that");
        mockMvc.perform(post("/events/globalmessage").contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionIsAdmin(any()));
    }

    @Test
    void postDemoEvent_invalidBody_400Response() throws Exception {
        var json = new JSONObject();
        json.put("foo", "bar");
        mockMvc.perform(post("/events/globalmessage").contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void postDemoEvent_validRequest_notifiedAllUsers() throws Exception {
        var json = new JSONObject();
        json.put("message", "this that");
        mockMvc.perform(post("/events/globalmessage").contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository).findAll();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<User>> userCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<MessageEvent> eventCaptor = ArgumentCaptor.forClass(MessageEvent.class);

        verify(eventService).addUsersToEvent(userCaptor.capture(), eventCaptor.capture());

        assertEquals(Set.of(mockUser), userCaptor.getValue());
        assertEquals("this that", eventCaptor.getValue().getMessage());
    }

    @Test
    void eventEmitter_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        mockMvc.perform(get("/events/emitter").queryParam("userId", "7"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void eventEmitter_differentUser_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);

        mockMvc.perform(get("/events/emitter").queryParam("userId", "7"))
                .andExpect(status().isForbidden())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L)));
    }

    @Test
    void eventEmitter_userNotFound_406Response() throws Exception {
        // 406 Should only be possible if user is admin
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(999L))).thenReturn(true);

        mockMvc.perform(get("/events/emitter").queryParam("userId", "999"))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(userRepository).findById(999L);
    }

    @Test
    void eventEmitter_validRequest_emitterGenerated() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        mockMvc.perform(get("/events/emitter").queryParam("userId", "7"))
                .andExpect(status().isOk())
                .andReturn();

        verify(eventService).createEmitterForUser(mockUser);
    }
}
