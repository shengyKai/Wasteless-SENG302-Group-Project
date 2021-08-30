package org.seng302.leftovers.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.seng302.leftovers.dto.event.Tag;
import org.seng302.leftovers.entities.event.Event;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    @ParameterizedTest
    @EnumSource(Tag.class)
    void constructJSONObject_withProvidedTag_tagReturnedInJson(Tag tag) {
        Event event = new EventSubclass();
        event.setTag(tag);

        var json = event.constructJSONObject();
        assertEquals(tag.toString().toLowerCase(), json.get("tag"));
    }

    @Test
    void setTag_nullTag_400Response() {
        Event event = new EventSubclass();

        var exception = assertThrows(ResponseStatusException.class, () -> event.setTag(null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Tag cannot be null", exception.getReason());
    }

    @Test
    void eventCreated_noTagProvided_tagIsNone() {
        Event event = new EventSubclass();
        assertEquals(Tag.NONE, event.getTag());
    }

    @Test
    void constructJSONObject_any_validFields() {
        Event event = new EventSubclass();

        var json = event.constructJSONObject();
        assertNull(json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals(event.getClass().getSimpleName(), json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals(4, json.size());
    }

    // Event is abstract so we need to subclass
    static class EventSubclass extends Event {}
}
