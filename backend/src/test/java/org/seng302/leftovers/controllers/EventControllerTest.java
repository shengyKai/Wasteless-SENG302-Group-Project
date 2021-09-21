package org.seng302.leftovers.controllers;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class EventControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private User mockUser;

    @Mock
    private Event mockEvent1;
    @Mock
    private Event mockEvent2;
    @Mock
    private EventDTO mockEventDTO1;
    @Mock
    private EventDTO mockEventDTO2;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private EventController eventController;

    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Make sure that user is admin by default
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        when(mockUser.getUserID()).thenReturn(7L);

        when(mockEvent1.getNotifiedUser()).thenReturn(mockUser);

        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userRepository.findById(7L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(not(eq(7L)))).thenReturn(Optional.empty());

        when(eventRepository.findById(2L)).thenReturn(Optional.of(mockEvent1));
        when(eventRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());
        when(eventRepository.findEventsForUser(mockUser)).thenReturn(List.of(mockEvent1, mockEvent2));
        when(eventRepository.findEventsForUser(eq(mockUser), any())).thenReturn(List.of(mockEvent1, mockEvent2));

        when(mockEvent1.asDTO()).thenReturn(mockEventDTO1);
        when(mockEvent2.asDTO()).thenReturn(mockEventDTO2);

        eventController = new EventController(userRepository, eventRepository);
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
                .thenThrow(new AccessTokenResponseException());

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
        verify(eventRepository, times(0)).save(any());
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
        verify(eventRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @EnumSource(EventTag.class)
    void setEventTag_validTag_200ResponseAndTagUpdated(EventTag eventTag) throws Exception {
        var json = new JSONObject();
        json.put("value", eventTag.toString().toLowerCase());
        mockMvc.perform(
                put("/feed/2/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockEvent1, times(1)).setTag(eventTag);
        verify(eventRepository, times(1)).save(mockEvent1);
    }

    @Test
    void postDemoEvent_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

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

        ArgumentCaptor<GlobalMessageEvent> eventCaptor = ArgumentCaptor.forClass(GlobalMessageEvent.class);

        verify(eventRepository).save(eventCaptor.capture());

        assertEquals("this that", eventCaptor.getValue().getGlobalMessage());
        assertEquals(mockUser, eventCaptor.getValue().getNotifiedUser());
    }

    @Test
    void getEvents_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        mockMvc.perform(get("/users/7/feed"))
                .andExpect(status().isUnauthorized());

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void getEvents_differentUser_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(false);

        mockMvc.perform(get("/users/7/feed"))
                .andExpect(status().isForbidden());

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L)));
    }

    @Test
    void getEvents_userNotFound_406Response() throws Exception {
        // 406 Should only be possible if user is admin
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(999L))).thenReturn(true);

        mockMvc.perform(get("/users/999/feed"))
                .andExpect(status().isNotAcceptable());

        verify(userRepository).findById(999L);
    }

    @Test
    void getEvents_invalidModifySinceParam_404Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        mockMvc.perform(get("/users/7/feed").param("modifiedSince", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvents_validRequestAndNoModifiedSinceParam_200Response() throws Exception {
        when(eventRepository.findEventsForUser(mockUser)).thenReturn(List.of());
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        mockMvc.perform(get("/users/7/feed"))
                .andExpect(status().isOk());

        verify(eventRepository).findEventsForUser(mockUser);
    }

    @Test
    void getEvents_validRequestAndNoModifiedSinceParam_allEventsReturned() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        // Method has to be called directly instead of using mockMvc to allow mocking of events and DTOs
        var result = eventController.getEvents(7L, null, mockRequest, mockResponse);

        verify(eventRepository).findEventsForUser(mockUser);
        assertEquals(result, List.of(mockEventDTO1, mockEventDTO2));
    }

    @Test
    void getEvents_validRequestAndValidModifiedSinceParam_200Response() throws Exception {
        when(eventRepository.findEventsForUser(mockUser, Instant.parse("2021-09-08T08:47:59.018528Z"))).thenReturn(List.of());
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        mockMvc.perform(get("/users/7/feed").param("modifiedSince", "2021-09-08T08:47:59.018528Z"))
                .andExpect(status().isOk());

        verify(eventRepository).findEventsForUser(mockUser, Instant.parse("2021-09-08T08:47:59.018528Z"));
    }

    @Test
    void getEvents_validRequestAndValidModifiedSinceParam_allEventsReturned() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(true);

        // Method has to be called directly instead of using mockMvc to allow mocking of events and DTOs
        var result = eventController.getEvents(7L, "2021-09-08T08:47:59.018528Z", mockRequest, mockResponse);

        verify(eventRepository).findEventsForUser(mockUser, Instant.parse("2021-09-08T08:47:59.018528Z"));
        assertEquals(result, List.of(mockEventDTO1, mockEventDTO2));
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
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenResponseException());
        mockMvc.perform(
                delete("/feed/10"))
                .andExpect(status().isUnauthorized());
        verify(eventRepository, times(0)).delete(any());
    }

    @Test
    void updateEventAsRead_eventExists_eventMarkedAsRead() throws Exception {
        mockMvc.perform(put("/feed/2/read"))
                .andExpect(status().isOk())
                .andReturn();
        verify(mockEvent1, times(1)).markAsRead();
    }

    @Test
    void updateEventStatus_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        var json = new JSONObject();
        json.put("value", "normal");
        mockMvc.perform(
                put("/feed/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void updateEventStatus_eventDoesNotExist_406Response() throws Exception {
        var json = new JSONObject();
        json.put("value", "normal");
        mockMvc.perform(
                put("/feed/9999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        // Check that the event repository was queried
        verify(eventRepository, times(1)).findById(9999L);
    }

    @Test
    void updateEventStatus_notAuthorised_403Response() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L))).thenReturn(false);

        var json = new JSONObject();
        json.put("value", "normal");
        mockMvc.perform(
                put("/feed/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        // Check that the authentication token managed was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(7L)));
    }

    @Test
    void updateEventStatus_noStatusProvided_400Response() throws Exception {
        var json = new JSONObject();
        json.put("other", "foo");
        mockMvc.perform(
                put("/feed/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void updateEventStatus_invalidStatus_400Response() throws Exception {
        var json = new JSONObject();
        json.put("value", "invalid");
        mockMvc.perform(
                put("/feed/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(eventRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void updateEventStatus_cannotChangeStatus_400Response(EventStatus newStatus) throws Exception {
        var json = new JSONObject();
        json.put("value", newStatus.toString().toLowerCase());
        when(mockEvent1.getStatus()).thenReturn(EventStatus.ARCHIVED);
        mockMvc.perform(
                put("/feed/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockEvent1, times(0)).updateEventStatus(newStatus);
        verify(eventRepository, times(0)).save(any());
    }


    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void updateEventStatus_canChangeStatusAndValidNewStatus_200ResponseAndStatusUpdated(EventStatus newStatus) throws Exception {
        var json = new JSONObject();
        json.put("value", newStatus.toString().toLowerCase());
        when(mockEvent1.getStatus()).thenReturn(EventStatus.NORMAL);
        mockMvc.perform(
                put("/feed/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockEvent1, times(1)).updateEventStatus(newStatus);
        verify(eventRepository, times(1)).save(mockEvent1);
    }
}
