package org.seng302.leftovers.service;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.GlobalMessageEvent;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.EventRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private User mockUser;

    private SseEmitter.SseEventBuilder mockBuilder;

    private MockedConstruction<SseEmitter> emitterMockedConstruction;

    private EventService eventService;
    private MockedStatic<SseEmitter> emitterMockedStatic;


    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        when(mockUser.getUserID()).thenReturn(42L);

        mockBuilder = mock(SseEmitter.SseEventBuilder.class, withSettings().defaultAnswer(RETURNS_SELF));

        emitterMockedConstruction = mockConstruction(SseEmitter.class);
        emitterMockedStatic = mockStatic(SseEmitter.class);
        // Return a mock SseEventBuilder when SseEvent.event() is called
        emitterMockedStatic.when(() -> SseEmitter.event()).thenReturn(mockBuilder);

        eventService = new EventServiceImpl(eventRepository);
    }

    @AfterEach
    void tearDown() {
        emitterMockedConstruction.close();
        emitterMockedStatic.close();
    }


    /**
     * Checks that a emitter has sent the provided event.
     * This will not work if multiple events have been sent
     * @param emitter Emitter to check has sent the event
     * @param event Event to have been sent
     */
    private void verifyEmitterHasSentEvent(SseEmitter emitter, Event event) {
        assertDoesNotThrow(() -> verify(emitter, times(1)).send(mockBuilder));
        verify(mockBuilder, times(1)).name("newsfeed");
        verify(mockBuilder, times(1)).data(event.constructJSONObject(), MediaType.APPLICATION_JSON);
    }

    @Test
    void createEmitterForUser_userWithNoEvents_noEventsEmitted() throws IOException {
        when(eventRepository.getAllByNotifiedUserOrderByCreated(mockUser)).thenReturn(List.of());

        SseEmitter emitter = eventService.createEmitterForUser(mockUser);

        verify(emitter, times(0)).send(any());
    }

    @Test
    void createEmitterForUser_userWithNoEvents_hasAssignedEmitterCallbacks() {
        when(eventRepository.getAllByNotifiedUserOrderByCreated(mockUser)).thenReturn(List.of());

        SseEmitter emitter = eventService.createEmitterForUser(mockUser);

        verify(emitter).onCompletion(any());
        verify(emitter).onTimeout(any());
    }

    @Test
    void createEmitterForUser_userWithOneEvent_emitsEventWithSettings() throws IOException {
        Event event = new GlobalMessageEvent(mockUser,"foo");

        when(eventRepository.getAllByNotifiedUserOrderByCreated(mockUser)).thenReturn(List.of(event)); // Add initial event

        SseEmitter emitter = eventService.createEmitterForUser(mockUser);

        verifyEmitterHasSentEvent(emitter, event);
    }

    @Test
    void addUsersToEvent_saveEvent_userNotifiedOfEvent() throws IOException {
        // There's no real way around this implicit dependency
        SseEmitter emitter = eventService.createEmitterForUser(mockUser);
        verify(emitter, times(0)).send(any());

        Event event = mock(Event.class);

        when(event.getNotifiedUser()).thenReturn(mockUser);

        // Make sure that event is send-able
        var mockEventJson = new JSONObject();
        mockEventJson.put("foo", "bar");
        when(event.constructJSONObject()).thenReturn(mockEventJson);

        when(eventRepository.save(event)).thenReturn(event);

        assertEquals(event, eventService.saveEvent( event)); // Call function

        verify(eventRepository).save(event);

        verifyEmitterHasSentEvent(emitter, event);
    }
}
