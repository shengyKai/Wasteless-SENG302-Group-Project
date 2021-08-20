package org.seng302.leftovers.entities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConversationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private User seller;
    private User buyer;
    private User bystander;
    private MarketplaceCard testCard1;
    private MarketplaceCard testCard2;

    @BeforeAll
    void init() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        seller = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("6435550129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        seller = userRepository.save(seller);
        testCard1 = new MarketplaceCard.Builder()
                .withCreator(seller)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("Card Title")
                .build();
        testCard1 = marketplaceCardRepository.save(testCard1);
        testCard2 = new MarketplaceCard.Builder()
                .withCreator(seller)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("Card Title")
                .build();
        testCard2 = marketplaceCardRepository.save(testCard2);

        buyer = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("Joe")
                .withLastName("Bloggs")
                .withNickName("Dave")
                .withEmail("dave@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        buyer = userRepository.save(buyer);

        bystander = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("Davidson")
                .withLastName("Smith")
                .withNickName("Bobby")
                .withEmail("bobbysmith99@gmail.com")
                .withPassword("1440-H%nt3r2")
                .withBio("Likes slow walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        bystander = userRepository.save(bystander);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByCardAndBuyer_conversationDoesNotExist_emptyReturned() {
        assertTrue(conversationRepository.findByCardAndBuyer(testCard1, buyer).isEmpty());
    }

    @Test
    @Transactional
    void findByCardAndBuyer_conversationExists_correctConversationReturned() {
        var expectedConversation = conversationRepository.save(new Conversation(testCard1, buyer));

        // Red herrings
        conversationRepository.save(new Conversation(testCard2, buyer));
        conversationRepository.save(new Conversation(testCard1, bystander));

        var actualConversation = conversationRepository.findByCardAndBuyer(testCard1, buyer)
                .orElseThrow();

        assertEquals(expectedConversation.getId(), actualConversation.getId());
        assertEquals(expectedConversation.getCard().getID(), actualConversation.getCard().getID());
        assertEquals(expectedConversation.getBuyer().getUserID(), actualConversation.getBuyer().getUserID());
    }

    @Test
    void findConversation_conversationWithMessages_messagesReturnedAndOrderedCorrectly() throws Exception {
        var conversation = conversationRepository.save(new Conversation(testCard1, buyer));

        Instant pastInstant = Instant.now().minus(Duration.ofHours(1));
        Field created = Message.class.getDeclaredField("created");
        created.setAccessible(true);

        var message1 = new Message(conversation, buyer, "Gimme your avocados!");
        created.set(message1, pastInstant);
        var message2 = new Message(conversation, seller, "Why should I?");
        created.set(message2, pastInstant.plus(3, ChronoUnit.MINUTES));
        var message3 = new Message(conversation, buyer, "I'll make you an offer you can't refuse...");
        created.set(message3, pastInstant.plus(4, ChronoUnit.MINUTES));

        message1 = messageRepository.save(message1);
        message3 = messageRepository.save(message3);
        message2 = messageRepository.save(message2);


        List<Long> expectedMessageIds = List.of(message3.getId(), message2.getId(), message1.getId());

        List<Long> actualMessageIds;
        try (Session session = sessionFactory.openSession()) {
            conversation = session.find(Conversation.class, conversation.getId());
            actualMessageIds = conversation.getMessages().stream().map(Message::getId).collect(Collectors.toList());
        }

        assertEquals(expectedMessageIds, actualMessageIds); // Checks order as well
    }

    @Test
    void deleteConversation_conversationWithMessages_messagesDeleted() {
        var conversation = conversationRepository.save(new Conversation(testCard1, buyer));

        var message1 = messageRepository.save(new Message(conversation, buyer, "Gimme your pears!"));
        var message2 = messageRepository.save(new Message(conversation, seller, "Not this shit again"));

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            conversation = session.find(Conversation.class, conversation.getId());
            session.delete(conversation);
            session.getTransaction().commit();
        }

        assertFalse(messageRepository.existsById(message1.getId()));
        assertFalse(messageRepository.existsById(message2.getId()));
    }
}
