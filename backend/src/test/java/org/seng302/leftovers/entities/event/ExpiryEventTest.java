package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.event.ExpiryEventRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ExpiryEventTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private ExpiryEventRepository expiryEventRepository;
    @Autowired
    private BusinessRepository businessRepository;

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
        testCard = marketplaceCardRepository.save(testCard);
    }

    @Test
    void asDTO_jsonHasExpectedFormat() throws JsonProcessingException {
        ExpiryEvent testEvent = new ExpiryEvent(testCard);
        expiryEventRepository.save(testEvent);
        String expectedJsonString = String.format("{\"id\":%d," +
                "\"created\":\"%s\"," +
                "\"tag\":\"none\"," +
                "\"type\":\"ExpiryEvent\"," +
                "\"status\":\"%s\"," +
                "\"read\": %b," +
                "\"card\":%s," +
                "\"lastModified\": \"%s\"}",
                testEvent.getId(),
                testEvent.getCreated(),
                testEvent.getStatus().toString().toLowerCase(),
                testEvent.isRead(),
                testCard.constructJSONObject().toJSONString(),
                testEvent.getLastModified().toString());
        String actualJsonString = mapper.writeValueAsString(testEvent.asDTO());
        assertEquals(mapper.readTree(expectedJsonString), mapper.readTree(actualJsonString));
    }

}