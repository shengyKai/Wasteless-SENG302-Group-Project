package org.seng302.leftovers.controllers;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.entities.Tag;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.AccessTokenException;
import org.seng302.leftovers.persistence.EventRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.EventService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
class EventControllerTest {
    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private User mockUser;

    @Mock
    private Event mockEvent;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Make sure that user is admin by default
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        when(mockUser.getUserID()).thenReturn(7L);

        when(mockEvent.getNotifiedUser()).thenReturn(mockUser);

        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userRepository.findById(eq(7L))).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(not(eq(7L)))).thenReturn(Optional.empty());

        when(eventRepository.findById(eq(2L))).thenReturn(Optional.of(mockEvent));
        when(eventRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());


        EventController eventController = new EventController(userRepository, eventService, eventRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @AfterEach
    void tearDown() {
        authenticationTokenManager.close();
    }


    @Test
    void setEventTag_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        var json = new JSONObject();
        json.put("value", "none");
        mockMvc.perform(
                put("/feed/1/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void setEventTag_eventDoesNotExist_406Response() throws Exception {
        var json = new JSONObject();
        json.put("value", "none");
        mockMvc.perform(
                put("/feed/9999/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        // Check that the event repository was queried
        verify(eventRepository, times(1)).findById(9999L);
    }

    @Test
    void setEventTag_notAuthorised_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(false);

        var json = new JSONObject();
        json.put("value", "none");
        mockMvc.perform(
                put("/feed/2/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        // Check that the authentication token managed was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L)));
    }

    @Test
    void setEventTag_noTagProvided_400Response() throws Exception {
        var json = new JSONObject();
        json.put("other", "foo");
        mockMvc.perform(
                put("/feed/2/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(eventService, times(0)).saveEvent(any());
    }

    @Test
    void setEventTag_invalidTag_400Response() throws Exception {
        var json = new JSONObject();
        json.put("value", "invalid");
        mockMvc.perform(
                put("/feed/2/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(eventService, times(0)).saveEvent(any());
    }

    @ParameterizedTest
    @EnumSource(Tag.class)
    void setEventTag_validTag_20ResponseAndTagUpdated(Tag tag) throws Exception {
        var json = new JSONObject();
        json.put("value", tag.toString().toLowerCase());
        mockMvc.perform(
                put("/feed/2/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockEvent, times(1)).setTag(tag);
        verify(eventService, times(1)).saveEvent(mockEvent);
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
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<MessageEvent> eventCaptor = ArgumentCaptor.forClass(MessageEvent.class);

        verify(eventService).saveEvent(eventCaptor.capture());

        assertEquals("this that", eventCaptor.getValue().getMessage());
        assertEquals(mockUser, eventCaptor.getValue().getNotifiedUser());
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

    @Test
    void deleteEvent_validIdAndUser_200ResponseAndEventDeleted() throws Exception {
        mockMvc.perform(
                delete("/feed/2"))
                .andExpect(status().isOk());

        verify(eventRepository, times(1)).delete(any());
    }

    @Test
    void deleteEvent_doesNotHavePermission_403ResponseAndNoEventDeleted() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);
        mockMvc.perform(
                delete("/feed/2"))
                .andExpect(status().isForbidden());
        verify(eventRepository, times(0)).delete(any());
    }

    @Test
    void deleteEvent_eventOnFeedDoesNotExist_406ResponseAndNoEventDeleted() throws Exception {
        mockMvc.perform(
                delete("/feed/10"))
                .andExpect(status().isNotAcceptable());
        verify(eventRepository, times(0)).delete(any());
    }

    @Test
    void deleteEvent_noAuthToken_401ResponseAndNoEventDeleted() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());
        mockMvc.perform(
                delete("/feed/10"))
                .andExpect(status().isUnauthorized());
        verify(eventRepository, times(0)).delete(any());
    }
}
