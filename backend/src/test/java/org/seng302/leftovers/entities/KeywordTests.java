package org.seng302.leftovers.entities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeywordTests {
    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    KeywordRepository keywordRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SessionFactory sessionFactory;

    User testUser;

    @BeforeAll
    void initialise() {
        businessRepository.deleteAll(); // If a previous test hasn't cleaned up
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        keywordRepository.deleteAll();
    }

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        keywordRepository.deleteAll();
    }

    @Test
    void getAllByKeywords_marketplaceCardWithKeywordExists_findsCard() {
        var keyword = new Keyword("keywordName");
        keyword = keywordRepository.save(keyword);

        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .addKeyword(keyword)
                .build();
        card = marketplaceCardRepository.save(card);

        List<MarketplaceCard> cards = marketplaceCardRepository.getAllByKeywords(keyword);
        assertEquals(1, cards.size());

        MarketplaceCard foundCard = cards.get(0);
        assertEquals(card.getID(), foundCard.getID());
        assertEquals(card.getTitle(), foundCard.getTitle());
        assertEquals(card.getDescription(), foundCard.getDescription());
    }

    @Test
    void getAllByKeywords_marketplaceCardWithKeywordManyExist_findsManyCardsNoDuplicates() {
        var keyword = new Keyword("keywordName");
        keyword = keywordRepository.save(keyword);

        // Check for duplicates
        var otherKeyword = new Keyword("otherKeywordName");
        otherKeyword = keywordRepository.save(otherKeyword);

        List<MarketplaceCard> cards = new ArrayList<>();
        for (int i = 0; i<10; i++) {
            var card = new MarketplaceCard.Builder()
                    .withCreator(testUser)
                    .withSection(MarketplaceCard.Section.EXCHANGE)
                    .withTitle("test_title" + i)
                    .withDescription("test_description" + i)
                    .addKeyword(keyword)
                    .addKeyword(otherKeyword)
                    .build();
            card = marketplaceCardRepository.save(card);
            cards.add(card);
        }

        List<MarketplaceCard> foundCards = marketplaceCardRepository.getAllByKeywords(keyword);
        for (MarketplaceCard foundCard : foundCards) {
            assertTrue(cards.stream().anyMatch(card -> card.getID().equals(foundCard.getID())));
        }
        assertEquals(10, foundCards.size());
    }

    @Test
    void getAllByCards_marketplaceCardWithKeyword_findsKeyword() {
        var keyword = new Keyword("keywordName");
        keyword = keywordRepository.save(keyword);

        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .addKeyword(keyword)
                .build();
        card = marketplaceCardRepository.save(card);

        List<Keyword> keywords = keywordRepository.getAllByCards(card);
        assertEquals(1, keywords.size());

        Keyword foundKeyword = keywords.get(0);
        assertEquals(keyword.getID(), foundKeyword.getID());
        assertEquals(keyword.getName(), foundKeyword.getName());
    }

    @Test
    void getAllByCards_marketplaceCardWithManyKeywordsFromAddKeywords_findsKeywords() {
        var keywords = List.of(
                keywordRepository.save(new Keyword("keywordNameA")),
                keywordRepository.save(new Keyword("keywordNameB"))
        );

        var card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .addKeywords(keywords)
                .build();
        card = marketplaceCardRepository.save(card);

        List<Keyword> foundKeywords = keywordRepository.getAllByCards(card);
        assertEquals(2, keywords.size());

        for (Keyword foundKeyword : foundKeywords) {
            assertTrue(keywords.stream().anyMatch(keyword -> keyword.getID().equals(foundKeyword.getID())));
        }
    }

    @Test
    void getAllByCards_cardHasManyKeywords_findsManyKeywordsNoDuplicates() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description");

        List<Keyword> keywords = new ArrayList<>();
        for (char c : "abcdefghij".toCharArray()) {
            var keyword = new Keyword("keywordName" + c);
            keyword = keywordRepository.save(keyword);
            builder.addKeyword(keyword);
            keywords.add(keyword);
        }

        var card = marketplaceCardRepository.save(builder.build());

        // Save a potential duplicate source
        marketplaceCardRepository.save(
            new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .addKeyword(keywords.get(0))
                .build()
        );

        List<Keyword> foundKeywords = keywordRepository.getAllByCards(card);
        for (Keyword foundKeyword : foundKeywords) {
            assertTrue(keywords.stream().anyMatch(keyword -> keyword.getID().equals(foundKeyword.getID())));
        }
        assertEquals(10, foundKeywords.size());
    }

    @Test
    void keywordRepositorySave_duplicateKeywordNames_failsToSave() {
        var keyword0 = new Keyword("keywordName");
        assertDoesNotThrow(() -> keywordRepository.save(keyword0));

        var keyword1 = new Keyword("keywordName");
        assertThrows(DataIntegrityViolationException.class, () -> keywordRepository.save(keyword1));
    }

    @Test
    void keywordRepositorySave_multipleKeywords_differentIds() {
        var keyword0 = keywordRepository.save(new Keyword("keywordNameA"));
        var keyword1 = keywordRepository.save(new Keyword("keywordNameB"));

        assertNotEquals(keyword0.getID(), keyword1.getID());
    }

    @Test
    void keywordConstructor_nullName_throws400Exception() {
        var exception = assertThrows(ValidationResponseException.class, () -> new Keyword(null));
        assertEquals("Keyword name must be provided", exception.getMessage());
    }

    @Test
    void keywordConstructor_emptyName_throws400Exception() {
        var exception = assertThrows(ValidationResponseException.class, () -> new Keyword(""));
        assertEquals("Keyword name must be between 1-25 characters long", exception.getMessage());
    }

    @Test
    void keywordConstructor_tooLongName_throws400Exception() {
        String name = "a".repeat(26);

        var exception = assertThrows(ValidationResponseException.class, () -> new Keyword(name));
        assertEquals("Keyword name must be between 1-25 characters long", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {",", ".", "+", "\uD83D\uDE02", "\uFFFF"})
    void keywordConstructor_invalidCharacters_throws400Exception(String name) {
        var exception = assertThrows(ValidationResponseException.class, () -> new Keyword(name));
        assertEquals("Keyword name must only contain letters", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "A", "é", "树"})
    void keywordConstructor_validCharacters_createsSuccessfully(String name) {
        assertDoesNotThrow(() -> new Keyword(name));
    }

    @Test
    void findByOrderByNameAsc_keywordsExist_keywordsReturnedOrderedByName() {
        List<String> keywordNames = List.of("Cat", "Fish", "Dog", "Zebra", "Rabbit");

        List<Keyword> keywords = keywordNames.stream().map(Keyword::new).collect(Collectors.toList());
        keywordRepository.saveAll(keywords);

        List<String> sortedNames = keywordNames.stream().sorted().collect(Collectors.toList());
        List<String> resultNames = keywordRepository.findByOrderByNameAsc().stream().map(Keyword::getName).collect(Collectors.toList());
        assertEquals(sortedNames, resultNames);
    }

    @Test
    void deleteKeyword_withCard_keywordRemovedFromCard() {
        Keyword bystander = keywordRepository.save(new Keyword("Bystander"));
        Keyword deleted = keywordRepository.save(new Keyword("Deleted"));

        MarketplaceCard card = new MarketplaceCard.Builder()
                .withSection(MarketplaceCard.Section.WANTED)
                .withCreator(testUser)
                .withTitle("Title")
                .withDescription("Description")
                .build();
        card.addKeyword(bystander);
        card.addKeyword(deleted);
        card = marketplaceCardRepository.save(card);

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            deleted = session.find(Keyword.class, deleted.getID());
            session.delete(deleted);

            session.getTransaction().commit();
        }

        card = marketplaceCardRepository.findById(card.getID()).orElseThrow();

        // Deleted keyword should have been removed from the card, but bystander keyword should stay
        assertEquals(1, card.getKeywords().size());
        assertEquals(bystander.getName(), card.getKeywords().get(0).getName());
    }

    @Test
    void newKeyword_strangeFormatting_nameFormatted() {
        Keyword formattedKeyword = new Keyword("dAnce iS   cOol");
        assertEquals("Dance Is Cool", formattedKeyword.getName());
    }

    @Test
    void newKeyword_spacesAround_spacesRemoved() {
        Keyword formattedKeyword = new Keyword(" Cool  ");
        assertEquals("Cool", formattedKeyword.getName());
    }

    @Test
    void newKeyword_extraCapitalisation_capitalisationRemoved() {
        Keyword formattedKeyword = new Keyword("THISthatOtheR");
        assertEquals("Thisthatother", formattedKeyword.getName());
    }
}
