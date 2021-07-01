package org.seng302.leftovers.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventTest {

    @Test
    void createEvent_any_creationTimeCorrect() {
        var before = Instant.now();
        Event event = new EventSubclass();
        var after = Instant.now();

        assertFalse(event.getCreated().isBefore(before));
        assertFalse(event.getCreated().isAfter(after));
    }

    @Test
    void constructJSONObject_any_validFields() {
        Event event = new EventSubclass();

        var json = event.constructJSONObject();
        assertNull(json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals(event.getClass().getSimpleName(), json.get("type"));
        assertEquals(3, json.size());
    }

    // Event is abstract so we need to subclass
    static class EventSubclass extends Event {}
}
