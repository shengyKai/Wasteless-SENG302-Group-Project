package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.dto.KeywordDTO;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class KeywordCreatedEventTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;

    private Keyword keyword;
    private User user;
    private User adminUser;

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
    void asDTO_jsonHasExpectedFormat() throws JsonProcessingException {
        KeywordCreatedEvent event = new KeywordCreatedEvent(adminUser, user, keyword);
        event = eventRepository.save(event);

        String expectedJsonString = String.format(
                        "{\"id\":%d," +
                        "\"created\":\"%s\"," +
                        "\"type\":\"KeywordCreatedEvent\"," +
                        "\"keyword\":%s," +
                        "\"tag\":\"none\"," +
                        "\"status\":\"%s\"," +
                        "\"read\": %b," +
                        "\"creator\":%s," +
                        "\"lastModified\":\"%s\"}",
                event.getId(),
                event.getCreated(),
                mapper.convertValue(new KeywordDTO(keyword), JSONObject.class),
                event.getStatus().toString().toLowerCase(),
                event.isRead(),
                mapper.writeValueAsString(new UserResponseDTO(user)),
                event.getLastModified().toString());
        System.out.println(expectedJsonString);
        String actualJsonString = mapper.writeValueAsString(event.asDTO());
        assertEquals(mapper.readTree(expectedJsonString), mapper.readTree(actualJsonString));
    }

}