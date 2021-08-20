package org.seng302.leftovers.entities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketplaceCardTests {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private User testUser;

    @BeforeAll
    void initialise() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() throws Exception{
        testUser = new User.Builder()
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
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void marketplaceCardDelayCloses_closesIsInsideOf1Day_delaysClosesByTwoWeeks() {
        var closes = Instant.now().plus(23, ChronoUnit.HOURS);
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();

        assertDoesNotThrow(() -> card.delayCloses());

        var expectedExtendedCloses = closes.plus(14, ChronoUnit.DAYS);
        assertEquals(expectedExtendedCloses, card.getCloses());
    }

    @Test
    void delayCloses_closesIsInsideOfOneDay_lastRenewedSetToCurrentTime() {
        var closes = Instant.now().plus(23, ChronoUnit.HOURS);
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();

        Instant before = Instant.now();
        card.delayCloses();
        Instant after = Instant.now();

        assertFalse(card.getLastRenewed().isBefore(before));
        assertFalse(card.getLastRenewed().isAfter(after));
    }

    @Test
    void marketplaceCardDelayCloses_closesIsOutsideOf1Day_throws400Exception() {
        var closes = Instant.now().plus(25, ChronoUnit.HOURS);
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();

        var exception = assertThrows(ResponseStatusException.class, () -> card.delayCloses());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Too early to extend closing date", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withParameters_propertiesSet() {
        var closes = Instant.now().plus(1000, ChronoUnit.DAYS);

        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();

        assertSame(testUser, card.getCreator());
        assertEquals(MarketplaceCard.Section.EXCHANGE, card.getSection());
        assertEquals("test_title", card.getTitle());
        assertEquals("test_description", card.getDescription());
        assertEquals(closes, card.getCloses());
    }

    @Test
    void marketplaceCardBuild_withParameters_creationTimeValid() {
        Instant before = Instant.now();
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();
        Instant after = Instant.now();

        assertFalse(card.getCreated().isBefore(before));
        assertFalse(card.getCreated().isAfter(after));
    }

    @Test
    void marketplaceCardBuild_withParameters_lastRenewedSameAsCreated() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();
        assertEquals(card.getCreated(), card.getLastRenewed());
    }

    @Test
    void marketplaceCardBuild_withoutCloses_generatesCloses() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();

        assertEquals(card.getCreated().plus(14, ChronoUnit.DAYS), card.getCloses());
    }

    @ParameterizedTest
    @EnumSource(MarketplaceCard.Section.class)
    void marketplaceCardBuild_withSectionName_parsesSection(MarketplaceCard.Section section) {
        String sectionName = section.getName();

        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(sectionName)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();

        assertEquals(section, card.getSection());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "not a section name"})
    void marketplaceCardBuild_withInvalidSectionName_throws400Exception(String sectionName) {
        var builder = new MarketplaceCard.Builder();

        var exception = assertThrows(ResponseStatusException.class, () -> builder.withSection(sectionName));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid section name", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withoutCreator_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card creator not provided", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withoutTitle_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withDescription("test_description");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card title must be provided", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withEmptyTitle_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("")
                .withDescription("test_description");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card title must be between 1-50 characters long", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withTooLongTitle_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("a".repeat(51))
                .withDescription("test_description");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card title must be between 1-50 characters long", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "\uD83D\uDE02", "\uFFFF"})
    void marketplaceCardBuild_withInvalidTitleCharacters_throws400Exception(String title) {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle(title)
                .withDescription("test_description");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card title must only contain letters, numbers, spaces and punctuation", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {":", ",", "7", "é", "树"})
    void marketplaceCardBuild_withValidTitleCharacters_createsCard(String title) {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle(title)
                .withDescription("test_description");
        var card = assertDoesNotThrow(builder::build);
        assertEquals(title, card.getTitle());
    }

    @Test
    void marketplaceCardBuild_withEmptyDescription_setsDescriptionToNull() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("")
                .build();
        assertNull(card.getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uD83D\uDE02", "\uFFFF"})
    void marketplaceCardBuild_withInvalidDescriptionCharacter_throws400Exception(String description) {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription(description);

        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card description must only contain letters, numbers, whitespace and punctuation", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\n", "\t",  ":", ",", "7", "é", "树"})
    void marketplaceCardBuild_withValidCharacters_createsCard(String description) {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription(description);
        var card = assertDoesNotThrow(builder::build);
        assertEquals(description, card.getDescription());
    }

    @Test
    void marketplaceCardBuild_withTooLongDescription_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("a".repeat(201));

        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Card description must not be longer than 200 characters", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withNullCloses_throws400Exception() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();

        var exception = assertThrows(ResponseStatusException.class, () -> card.setCloses(null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Closing time cannot be null", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withClosingInPast_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(Instant.now().minus(1, ChronoUnit.DAYS));
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Closing time cannot be before creation", exception.getReason());
    }

    @Test
    void marketplaceCardBuild_withNullKeyword_throws400Exception() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .addKeyword(null);
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Keyword cannot be null", exception.getReason());
    }

    @Test
    void marketplaceRepository_saveMultipleCards_differentIds() {
        var card1 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();
        var card2 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();
        card1 = marketplaceCardRepository.save(card1);
        card2 = marketplaceCardRepository.save(card2);

        assertNotEquals(card1.getID(), card2.getID());
    }

    @Test
    void marketplaceRepositoryFindById_saveCard_findsCard() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .build();
        card = marketplaceCardRepository.save(card);

        var foundCard = marketplaceCardRepository.findById(card.getID());
        assertTrue(foundCard.isPresent());
        assertEquals(card.getID(), foundCard.get().getID());
        assertEquals(card.getCreator().getUserID(), foundCard.get().getCreator().getUserID());
        assertEquals(card.getSection(), foundCard.get().getSection());
        assertEquals(card.getTitle(), foundCard.get().getTitle());
        assertEquals(card.getDescription(), foundCard.get().getDescription());
        assertEquals(0, ChronoUnit.SECONDS.between(card.getCreated(), foundCard.get().getCreated()));
        assertEquals(0, ChronoUnit.SECONDS.between(card.getCloses(), foundCard.get().getCloses()));
    }

    @Test
    void marketplaceRepository_saveCard_addsToUserCardsCreated() {
        var card1 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title1")
                .withDescription("test_description1")
                .build();
        card1 = marketplaceCardRepository.save(card1);
        var card2 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.FOR_SALE)
                .withTitle("test_title2")
                .withDescription("test_description2")
                .build();
        card2 = marketplaceCardRepository.save(card2);

        Set<Long> addedIds = Set.of(card1.getID(), card2.getID());

        List<MarketplaceCard> cardList = marketplaceCardRepository.getAllByCreator(testUser);
        Set<Long> foundIds = cardList.stream().map(MarketplaceCard::getID).collect(Collectors.toSet());

        assertEquals(addedIds, foundIds);
    }

    @Test
    void marketplaceCardRepository_saveMultipleCards_differentIds() {
        var card1 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title1")
                .withDescription("test_description1")
                .build();
        card1 = marketplaceCardRepository.save(card1);
        var card2 = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.FOR_SALE)
                .withTitle("test_title2")
                .withDescription("test_description2")
                .build();
        card2 = marketplaceCardRepository.save(card2);

        assertNotEquals(card1.getID(), card2.getID());
    }

    @Test
    void marketplaceCardRepositoryGetCard_cardDoesNotExist_throws404Exception() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title1")
                .withDescription("test_description1")
                .build();
        card = marketplaceCardRepository.save(card);
        marketplaceCardRepository.delete(card);
        Long id = card.getID();

        var exception = assertThrows(ResponseStatusException.class, () -> marketplaceCardRepository.getCard(id));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("No card exists with the given id", exception.getReason());
    }

    @Test
    void marketplaceCardRepositoryGetCard_cardExists_cardReturned() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title1")
                .withDescription("test_description1")
                .build();
        card = marketplaceCardRepository.save(card);
        Long id = card.getID();

        var foundCard = assertDoesNotThrow(() -> marketplaceCardRepository.getCard(id));

        // Valid contents
        assertEquals(card.getID(), foundCard.getID());
        assertEquals(card.getCreator().getUserID(), foundCard.getCreator().getUserID());
        assertEquals(card.getSection(), foundCard.getSection());
        assertEquals(card.getTitle(), foundCard.getTitle());
        assertEquals(card.getDescription(), foundCard.getDescription());
        assertEquals(0, ChronoUnit.SECONDS.between(card.getCreated(), foundCard.getCreated()));
        assertEquals(0, ChronoUnit.SECONDS.between(card.getCloses(), foundCard.getCloses()));
    }

    @Test
    void marketplaceCardRepositoryDeleteCard_cardHasConversation_conversationDeleted() {
        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title1")
                .withDescription("test_description1")
                .build();
        card = marketplaceCardRepository.save(card);
        var buyer = new User.Builder()
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

        var conversation = conversationRepository.save(new Conversation(card, buyer));
        var message = messageRepository.save(new Message(conversation, buyer, "Hey!"));

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            card = session.find(MarketplaceCard.class, card.getID());
            session.delete(card);
            session.getTransaction().commit();
        }

        assertFalse(conversationRepository.existsById(conversation.getId()));
        assertFalse(messageRepository.existsById(message.getId()));
    }
}
