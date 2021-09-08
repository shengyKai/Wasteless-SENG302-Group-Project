package org.seng302.leftovers.persistence;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
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
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventRepositoryTest {

    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserRepository userRepository;

    User testUser;
    User otherUser;
    Event testUserEvent1;
    Event testUserEvent2;
    Event testUserEvent3;
    Event otherUserEvent1;
    Instant beforeCreation;

    @BeforeEach
    void setUp() throws InterruptedException {
        testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("testUser@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        otherUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("otherUser@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);
        otherUser = userRepository.save(otherUser);

        beforeCreation = Instant.now().minus(Duration.ofNanos(1));
        testUserEvent1 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 1"));
        sleep(0, 1); // Sleep for one nanosecond between creating events to ensure that they have different creation dates
        testUserEvent2 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 2"));
        sleep(0, 1);
        testUserEvent3 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 3"));
        sleep(0, 1);
        otherUserEvent1 = eventRepository.save(new GlobalMessageEvent(otherUser, "Other user event 1"));
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findEventsForUser_noSinceModifiedDateGiven_onlyEventsForGivenUserReturned() {
        List<Event> queryResult = eventRepository.findEventsForUser(testUser);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertTrue(queryResult.contains(testUserEvent3));
        assertFalse(queryResult.contains(otherUserEvent1));
    }

    @Test
    void findEventsForUser_noSinceModifiedDateGiven_returnedEventsOrderedByDescendingCreatedDate() {
        List<Event> queryResult = eventRepository.findEventsForUser(testUser);

        for (int i = 0; i < queryResult.size() - 1; i++) {
            assertTrue(queryResult.get(i).getCreated().isAfter(queryResult.get(i+1).getCreated()));
        }
    }

    @Test
    void findEventsForUser_sinceModifiedDateGiven_onlyEventsForGivenUserReturned() {
        List<Event> queryResult = eventRepository.findEventsForUser(testUser, beforeCreation);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertTrue(queryResult.contains(testUserEvent3));
        assertFalse(queryResult.contains(otherUserEvent1));
    }

    @Test
    void findEventsForUser_sinceModifiedDateGiven_returnedEventsOrderedByDescendingCreatedDate() {
        List<Event> queryResult = eventRepository.findEventsForUser(testUser, beforeCreation);

        for (int i = 0; i < queryResult.size() - 1; i++) {
            assertTrue(queryResult.get(i).getCreated().isAfter(queryResult.get(i+1).getCreated()));
        }
    }

    @Test
    void findEventsForUser_allModifiedAfterSinceModified_allReturned() throws InterruptedException {
        Instant modifiedDate = Instant.now();
        sleep(0, 1);

        testUserEvent1.markAsRead();
        testUserEvent1 = eventRepository.save(testUserEvent1);
        testUserEvent2.markAsRead();
        testUserEvent2 = eventRepository.save(testUserEvent2);
        testUserEvent3.markAsRead();
        testUserEvent3 = eventRepository.save(testUserEvent3);

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedDate);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertTrue(queryResult.contains(testUserEvent3));
    }

    @Test
    void findEventsForUser_someModifiedAfterSinceModified_modifiedEventsReturned() throws InterruptedException {
        Instant modifiedDate = Instant.now();
        sleep(0, 1);

        testUserEvent1.markAsRead();
        testUserEvent1 = eventRepository.save(testUserEvent1);
        testUserEvent2.markAsRead();
        testUserEvent2 = eventRepository.save(testUserEvent2);

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedDate);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertFalse(queryResult.contains(testUserEvent3));
    }

    @Test
    void findEventsForUser_noneModifiedAfterSinceModified_noneReturned() throws InterruptedException {
        Instant modifiedDate = Instant.now();
        sleep(0, 1);

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedDate);

        assertFalse(queryResult.contains(testUserEvent1));
        assertFalse(queryResult.contains(testUserEvent2));
        assertFalse(queryResult.contains(testUserEvent3));
    }

    @Test
    @SneakyThrows
    void onUpdate_lastModifiedOneDayAgo_lastModifiedChangedToCurrentInstant() {
        Field lastModifiedField = Event.class.getDeclaredField("lastModified");
        lastModifiedField.setAccessible(true);

        // Set the last modified date of the event to a date in the past
        Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1L));
        lastModifiedField.set(testUserEvent1, oneDayAgo);
        assertEquals(oneDayAgo, testUserEvent1.getLastModified());

        Instant before = Instant.now();
        testUserEvent1.updateEventStatus(EventStatus.ARCHIVED);
        testUserEvent1 = eventRepository.save(testUserEvent1);
        Instant after = Instant.now();

        assertFalse(before.isAfter(testUserEvent1.getLastModified()));
        assertFalse(after.isBefore(testUserEvent1.getLastModified()));
    }

}