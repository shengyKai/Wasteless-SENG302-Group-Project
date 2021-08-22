package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KeywordCreatedEventTest {

    @Autowired
    KeywordRepository keywordRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    Keyword keyword;
    User user;
    User adminUser;

    @BeforeEach
    void setUp() {
        keyword = new Keyword("Test");
        keyword = keywordRepository.save(keyword);
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

        adminUser = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("David")
                .withLastName("Davidson")
                .withNickName("DDD")
                .withEmail("david@davidson.com")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        adminUser = userRepository.save(adminUser);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        keywordRepository.deleteAll();
    }

    @Test
    void constructJSONObject_jsonHasExpectedFormat() throws JsonProcessingException {
        KeywordCreatedEvent event = new KeywordCreatedEvent(adminUser, user, keyword);
        event = eventRepository.save(event);

        String expectedJsonString = String.format(
                        "{\"id\":%d," +
                        "\"created\":\"%s\"," +
                        "\"type\":\"KeywordCreatedEvent\"," +
                        "\"keyword\":%s," +
                        "\"tag\":\"none\"," +
                        "\"creator\":%s}",
                event.getId(),
                event.getCreated(),
                keyword.constructJSONObject().toJSONString(),
                user.constructPublicJson(false).toJSONString());
        String actualJsonString = event.constructJSONObject().toJSONString();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJsonString), mapper.readTree(actualJsonString));
    }

}