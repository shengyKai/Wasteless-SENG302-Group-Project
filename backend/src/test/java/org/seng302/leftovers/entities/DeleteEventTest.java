package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class DeleteEventTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    BusinessRepository businessRepository;
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
    void constructJSONObject_jsonHasExpectedFormat() throws JsonProcessingException {
        DeleteEvent event = new DeleteEvent(testCard);
        event = eventRepository.save(event);

        JSONObject json = event.constructJSONObject();

        assertEquals(event.getId(), json.get("id"));
        assertEquals(event.getCreated().toString(), json.get("created"));
        assertEquals("DeleteEvent", json.get("type"));
        assertEquals(testCard.getTitle(), json.get("title"));
        assertEquals(testCard.getSection().getName(), json.get("section"));
        assertEquals(5, json.size());
    }

}