package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.persistence.ExpiryEventRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpiryEventTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    ExpiryEventRepository expiryEventRepository;
    private MarketplaceCard testCard;

    @BeforeEach
    void setUp() {
        setUpTestCard();
    }

    @AfterEach
    void tearDown() {
        expiryEventRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void setUpTestCard() {
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
        testCard = marketplaceCardRepository.save(testCard);
    }

    @Test
    void constructJSONObject_jsonHasExpectedFormat() throws JsonProcessingException {
        ExpiryEvent testEvent = new ExpiryEvent(testCard);
        expiryEventRepository.save(testEvent);
        String expectedJsonString = String.format("{\"id\":%d," +
                "\"created\":\"%s\"," +
                "\"type\":\"ExpiryEvent\"," +
                "\"card\":%s}",
                testEvent.getId(),
                testEvent.getCreated(),
                testCard.constructJSONObject().toJSONString());
        String actualJsonString = testEvent.constructJSONObject().toJSONString();
        System.out.println(expectedJsonString);
        System.out.println(actualJsonString);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJsonString), mapper.readTree(actualJsonString));
    }

}