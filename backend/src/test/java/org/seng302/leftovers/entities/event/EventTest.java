package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.Duration;
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

        var exception = assertThrows(ValidationResponseException.class, () -> event.setTag(null));
        assertEquals("Tag cannot be null", exception.getMessage());
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
        assertEquals(event.getLastModified().toString(), json.get("lastModified"));
        assertEquals(7, json.size());
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

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void updateEventStatus_initialStatusNormal_eventStatusUpdated(EventStatus newStatus) {
        Event event = new EventSubclass();
        assertEquals(EventStatus.NORMAL, event.getStatus());

        event.updateEventStatus(newStatus);
        assertEquals(newStatus, event.getStatus());
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void updateEventStatus_initialStatusStarred_eventStatusUpdated(EventStatus newStatus) {
        Event event = new EventSubclass();
        event.updateEventStatus(EventStatus.STARRED);
        assertEquals(EventStatus.STARRED, event.getStatus());

        event.updateEventStatus(newStatus);
        assertEquals(newStatus, event.getStatus());
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void updateEventStatus_initialStatusArchived_eventStatusUnchanged(EventStatus newStatus) {
        Event event = new EventSubclass();
        event.updateEventStatus(EventStatus.ARCHIVED);
        assertEquals(EventStatus.ARCHIVED, event.getStatus());

        event.updateEventStatus(newStatus);
        assertEquals(EventStatus.ARCHIVED, event.getStatus());
    }

    @Test
    void createEvent_any_lastModifiedTimeCorrect() {
        var before = Instant.now();
        Event event = new EventSubclass();
        var after = Instant.now();

        assertFalse(event.getLastModified().isBefore(before));
        assertFalse(event.getLastModified().isAfter(after));
    }

    @Test
    @SneakyThrows
    void onUpdate_any_lastModifiedTimeUpdated() {
        Event event = new EventSubclass();

        Field lastModifiedField = Event.class.getDeclaredField("lastModified");
        lastModifiedField.setAccessible(true);

        // Set the last modified date of the event to a date in the past
        Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1L));
        lastModifiedField.set(event, oneDayAgo);
        assertEquals(oneDayAgo, event.getLastModified());

        var before = Instant.now();
        event.onUpdate();
        var after = Instant.now();

        assertFalse(event.getLastModified().isBefore(before));
        assertFalse(event.getLastModified().isAfter(after));
    }
}
