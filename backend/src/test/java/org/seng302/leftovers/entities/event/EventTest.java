package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventTest {
    @Autowired
    private ObjectMapper mapper;

    @Test
    void createEvent_any_creationTimeCorrect() {
        var before = Instant.now();
        Event event = new EventSubclass();
        var after = Instant.now();

        assertFalse(event.getCreated().isBefore(before));
        assertFalse(event.getCreated().isAfter(after));
    }

    @ParameterizedTest
    @EnumSource(EventTag.class)
    void eventDTO_withProvidedTag_tagReturnedInJson(EventTag eventTag) {
        Event event = new EventSubclass();
        event.setTag(eventTag);

        var json = mapper.convertValue(new EventDTO(event) {}, JSONObject.class);
        assertEquals(eventTag.toString().toLowerCase(), json.get("tag"));
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
        assertEquals(EventTag.NONE, event.getTag());
    }

    @Test
    void eventDTO_any_validFields() {
        Event event = new EventSubclass();

        var json = mapper.convertValue(event.asDTO(), JSONObject.class);
        assertNull(json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals(event.getClass().getSimpleName(), json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals(event.getStatus().toString().toLowerCase(), json.get("status"));
        assertEquals(event.isRead(), json.get("read"));
        assertEquals(6, json.size());
    }

    // Event is abstract so we need to subclass
    static class EventSubclass extends Event {
        @Override
        public EventDTO asDTO() {
            return new EventDTO(this) {};
        }
    }

    @Test
    void eventRead_updatesIsRead_isReadUpdatedToTrue() {
        Event event = new EventSubclass();
        assertEquals(false, event.isRead());
        event.markAsRead();
        assertEquals(true, event.isRead());
    }

    @Test
    void updateEventStatus_getDefaultStatus_defaultStatusIsNormal() {
        Event event = new EventSubclass();
        assertEquals(EventStatus.NORMAL, event.getStatus());
    }

    @Test
    void updateEventStatus_updateStatusToArchived_eventStatusIsArchived() {
        Event event = new EventSubclass();
        event.updateEventStatus(EventStatus.ARCHIVED);
        assertEquals(EventStatus.ARCHIVED, event.getStatus());
    }

    @Test
    void updateEventStatus_updateStatusToStarred_eventStatusIsStarred() {
        Event event = new EventSubclass();
        event.updateEventStatus(EventStatus.STARRED);
        assertEquals(EventStatus.STARRED, event.getStatus());
    }
}
