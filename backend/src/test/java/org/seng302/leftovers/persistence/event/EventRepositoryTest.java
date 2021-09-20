package org.seng302.leftovers.persistence.event;

import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventRepositoryTest {

    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserRepository userRepository;
    @Resource
    SessionFactory sessionFactory;

    User testUser;
    User otherUser;
    GlobalMessageEvent testUserEvent1;
    GlobalMessageEvent testUserEvent2;
    GlobalMessageEvent testUserEvent3;
    GlobalMessageEvent otherUserEvent1;
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

        testUserEvent1 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 1"));
        testUserEvent2 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 2"));
        testUserEvent3 = eventRepository.save(new GlobalMessageEvent(testUser, "Test user event 3"));
        otherUserEvent1 = eventRepository.save(new GlobalMessageEvent(otherUser, "Other user event 1"));
        
        // Give all events a different creation and lastModified date so sorting and filtering can be tested
        List<GlobalMessageEvent> events = List.of(testUserEvent1, testUserEvent2, testUserEvent3, otherUserEvent1);
        beforeCreation = Instant.parse("2021-09-10T12:00:00Z");
        for (int i = 0; i < events.size(); i++) {
            setCreatedForEventInDatabase(events.get(i), beforeCreation.plus(Duration.ofSeconds(i+1)));
            setLastModifiedForEventInDatabase(events.get(i), beforeCreation.plus(Duration.ofSeconds(i+1)));
        }
    }

    /**
     * This method sets the lastModified date of an event in the database using an SQL statement.
     * Setting the lastModified date directly in the database rather than through the event object
     * prevents the automatic onUpdate method of Event from being triggered.
     * @param event The event to be updated.
     * @param modifiedDate The date to set the event's lastModified date to.
     */
    private void setLastModifiedForEventInDatabase(GlobalMessageEvent event, Instant modifiedDate) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.createNativeQuery("UPDATE global_message_event SET last_modified = :lastModified WHERE id = :id")
                    .setParameter("lastModified", modifiedDate)
                    .setParameter("id", event.getId())
                    .executeUpdate();

            transaction.commit();
        }
    }

    /**
     * This method sets the created date of an event in the database using an SQL statement.
     * Setting the created date directly in the database rather than through the event object
     * prevents the automatic onUpdate method of Event from being triggered.
     * @param event The event to be updated.
     * @param created The date to set the event's created date to.
     */
    private void setCreatedForEventInDatabase(GlobalMessageEvent event, Instant created) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.createNativeQuery("UPDATE global_message_event SET created = :created WHERE id = :id")
                    .setParameter("created", created)
                    .setParameter("id", event.getId())
                    .executeUpdate();

            transaction.commit();
        }
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
    void findEventsForUser_allModifiedAfterSinceModified_allReturned() {
        Instant modifiedSinceDate = Instant.parse("2021-09-11T12:00:00Z");
        setLastModifiedForEventInDatabase(testUserEvent1, modifiedSinceDate.plus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent2, modifiedSinceDate.plus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent3, modifiedSinceDate.plus(Duration.ofSeconds(1)));

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedSinceDate);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertTrue(queryResult.contains(testUserEvent3));
    }

    @Test
    void findEventsForUser_someModifiedAfterSinceModified_modifiedEventsReturned() {
        Instant modifiedSinceDate = Instant.parse("2021-09-11T12:00:00Z");
        setLastModifiedForEventInDatabase(testUserEvent1, modifiedSinceDate.plus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent2, modifiedSinceDate.plus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent3, modifiedSinceDate.minus(Duration.ofSeconds(1)));

        List<Event> allEvents = eventRepository.findEventsForUser(testUser);
        System.out.println(allEvents.get(0).getLastModified());
        System.out.println(allEvents.get(1).getLastModified());
        System.out.println(allEvents.get(2).getLastModified());

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedSinceDate);

        assertTrue(queryResult.contains(testUserEvent1));
        assertTrue(queryResult.contains(testUserEvent2));
        assertFalse(queryResult.contains(testUserEvent3));
    }

    @Test
    void findEventsForUser_noneModifiedAfterSinceModified_noneReturned() {
        Instant modifiedSinceDate = Instant.parse("2021-09-11T12:00:00Z");
        setLastModifiedForEventInDatabase(testUserEvent1, modifiedSinceDate.minus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent2, modifiedSinceDate.minus(Duration.ofSeconds(1)));
        setLastModifiedForEventInDatabase(testUserEvent3, modifiedSinceDate.minus(Duration.ofSeconds(1)));

        List<Event> allEvents = eventRepository.findEventsForUser(testUser);
        System.out.println(allEvents.get(0).getLastModified());
        System.out.println(allEvents.get(1).getLastModified());
        System.out.println(allEvents.get(2).getLastModified());

        List<Event> queryResult = eventRepository.findEventsForUser(testUser, modifiedSinceDate);

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