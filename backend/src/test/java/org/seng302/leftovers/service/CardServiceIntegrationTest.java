package org.seng302.leftovers.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.MessageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CardServiceIntegrationTest {

    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    ConversationRepository conversationRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    MessageEventRepository messageEventRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CardService cardService;
    @Autowired
    MessageService messageService;
    User user;
    User buyer;
    Message message;
    Conversation conversation;
    MarketplaceCard card;

    @BeforeEach
    void setUp() {
        Location address = new Location.Builder()
                .inCity("city")
                .inCountry("New Zealand")
                .inRegion("region")
                .onStreet("street")
                .atStreetNumber("3")
                .withPostCode("222")
                .build();

        user = new User.Builder()
                .withEmail("user@user.com")
                .withFirstName("John")
                .withLastName("Smith")
                .withAddress(address)
                .withPassword("password123")
                .withDob("2000-08-04")
                .build();
        user = userRepository.save(user);
        card = new MarketplaceCard.Builder()
                .withCreator(user)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("Card")
                .build();
        card = marketplaceCardRepository.save(card);
    }

    @AfterEach
    void tearDown() {
        messageEventRepository.deleteAll();
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void delete_noConversation_marketplaceCardDeleted() {
        assertTrue(marketplaceCardRepository.findById(card.getID()).isPresent());

        assertDoesNotThrow(() -> cardService.deleteCardWithRelations(card));

        assertTrue(marketplaceCardRepository.findById(card.getID()).isEmpty());
    }

    @Test
    void delete_conversationExists_marketplaceCardAndConversationEntitiesDeleted() {
        addConversationToCard();

        assertTrue(marketplaceCardRepository.findById(card.getID()).isPresent());
        assertTrue(conversationRepository.findById(conversation.getId()).isPresent());
        assertTrue(messageRepository.findById(message.getId()).isPresent());
        assertTrue(messageEventRepository.findByNotifiedUserAndConversation(user, conversation).isPresent());
        assertTrue(messageEventRepository.findByNotifiedUserAndConversation(buyer, conversation).isPresent());

        assertDoesNotThrow(() -> cardService.deleteCardWithRelations(card));

        assertTrue(marketplaceCardRepository.findById(card.getID()).isEmpty());
        assertTrue(conversationRepository.findById(conversation.getId()).isEmpty());
        assertTrue(messageRepository.findById(message.getId()).isEmpty());
        assertTrue(messageEventRepository.findByNotifiedUserAndConversation(user, conversation).isEmpty());
        assertTrue(messageEventRepository.findByNotifiedUserAndConversation(buyer, conversation).isEmpty());
    }

    /**
     * Create a conversation with one message involving the test marketplace card
     */
    private void addConversationToCard() {
        Location address = new Location.Builder()
                .inCity("city")
                .inCountry("New Zealand")
                .inRegion("region")
                .onStreet("street")
                .atStreetNumber("3")
                .withPostCode("222")
                .build();

        buyer = new User.Builder()
                .withEmail("buyer@buyer.com")
                .withFirstName("John")
                .withLastName("Smith")
                .withAddress(address)
                .withPassword("password123")
                .withDob("2000-08-04")
                .build();
        buyer = userRepository.save(buyer);

        conversation = new Conversation(card, buyer);
        conversationRepository.save(conversation);

        message = new Message(conversation, buyer, "Hello");
        messageRepository.save(message);

        messageService.notifyConversationParticipants(message, buyer, card.getCreator());
    }

}
