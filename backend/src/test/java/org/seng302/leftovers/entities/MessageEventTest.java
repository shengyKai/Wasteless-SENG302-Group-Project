package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.leftovers.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageEventTest {

    @Autowired
    private EventRepository eventRepository;

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void createMessageEvent_nullParameter_400Response() {
        var exception = assertThrows(ResponseStatusException.class, () -> new MessageEvent(null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message cannot be null", exception.getReason());
    }

    @Test
    void createMessageEvent_someMessage_createsEventWithMessage() {
        var event = assertDoesNotThrow(() -> new MessageEvent("Foo"));
        assertEquals("Foo", event.getMessage());
    }

    @Test
    void constructJSONObject_withMessage_correctJson() {
        MessageEvent event = new MessageEvent("Foo");
        event = eventRepository.save(event); // Make sure to get an ID

        JSONObject json = event.constructJSONObject();
        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("MessageEvent", json.get("type"));
        assertEquals("Foo", json.get("message"));
        assertEquals(4, json.size());
    }
}
