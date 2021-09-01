package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.EventRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DeleteEventTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private MarketplaceCard testCard;

    @BeforeEach
    void setUp() {
        setUpTestCard();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void setUpTestCard() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        User testUser = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withDob("2000-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);
        testCard = new MarketplaceCard.Builder().
                withTitle("Test Card")
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.FOR_SALE)
                .build();
    }

    @Test
    void asDTO_jsonHasExpectedFormat() {
        DeleteEvent event = new DeleteEvent(testCard);
        event = eventRepository.save(event);

        var json = mapper.convertValue(event.asDTO(), JSONObject.class);

        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("DeleteEvent", json.get("type"));
        assertEquals("none", json.get("tag"));
        assertEquals(testCard.getTitle(), json.get("title"));
        assertEquals(testCard.getSection().getName(), json.get("section"));
        assertEquals("normal", json.get("eventStatus"));
        assertEquals(7, json.size());
    }

}