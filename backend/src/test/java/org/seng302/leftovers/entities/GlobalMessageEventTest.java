package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.*;
import org.seng302.leftovers.persistence.EventRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlobalMessageEventTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeAll
    void init() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        user = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        user = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createMessageEvent_nullMessage_400Response() {
        var exception = assertThrows(ResponseStatusException.class, () -> new GlobalMessageEvent(user, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message cannot be null", exception.getReason());
    }

    @Test
    void createMessageEvent_someMessage_createsEventWithMessage() {
        var event = assertDoesNotThrow(() -> new GlobalMessageEvent(user, "Foo"));
        assertEquals("Foo", event.getGlobalMessage());
    }

    @Test
    void constructJSONObject_withMessage_correctJson() {
        GlobalMessageEvent event = new GlobalMessageEvent(user, "Foo");
        event = eventRepository.save(event); // Make sure to get an ID

        JSONObject json = event.constructJSONObject();
        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("GlobalMessageEvent", json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals("Foo", json.get("message"));
        assertEquals(5, json.size());
    }
}