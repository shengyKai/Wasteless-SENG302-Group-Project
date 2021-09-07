package org.seng302.leftovers.persistence;

import io.cucumber.java.bs.A;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventRepositoryTest {

    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserRepository userRepository;

    User user;
    Event event;

    @BeforeEach
    void setUp() {
        user = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        user = userRepository.save(user);

        event = new GlobalMessageEvent(user, "Test message");
        event = eventRepository.save(event);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void onUpdate_lastModifiedOneDayAgo_lastModifiedChangedToCurrentInstant() {
        Field lastModifiedField = Event.class.getDeclaredField("lastModified");
        lastModifiedField.setAccessible(true);

        // Set the last modified date of the event to a date in the past
        Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1L));
        lastModifiedField.set(event, oneDayAgo);
        assertEquals(oneDayAgo, event.getLastModified());

        Instant before = Instant.now();
        event.updateEventStatus(EventStatus.ARCHIVED);
        event = eventRepository.save(event);
        Instant after = Instant.now();

        assertFalse(before.isAfter(event.getLastModified()));
        assertFalse(after.isBefore(event.getLastModified()));
    }

}