package org.seng302.entities;

import org.junit.jupiter.api.*;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeywordTests {
    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    KeywordRepository keywordRepository;

    @Autowired
    UserRepository userRepository;

    User testUser;

    @BeforeAll
    void initialise() {
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
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        keywordRepository.deleteAll();
    }

    @Test
    void getAllByKeywords_marketplaceCardWithKeywordExists_findsCard() {
        var keyword = new Keyword("keyword_name");
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
        var keyword = new Keyword("keyword_name");
        keyword = keywordRepository.save(keyword);

        // Check for duplicates
        var otherKeyword = new Keyword("other_keyword_name");
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
    void getAllByCards_marketplaceCardWithKeywordExists_findsKeyword() {
        var keyword = new Keyword("keyword_name");
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
    void getAllByCards_cardHasManyKeywords_findsManyKeywordsNoDuplicates() {
        var builder = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description");

        List<Keyword> keywords = new ArrayList<>();
        for (int i = 0; i<10; i++) {
            var keyword = new Keyword("keyword_name" + i);
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
        var keyword0 = new Keyword("keyword_name");
        assertDoesNotThrow(() -> keywordRepository.save(keyword0));

        var keyword1 = new Keyword("keyword_name");
        assertThrows(DataIntegrityViolationException.class, () -> keywordRepository.save(keyword1));
    }

    @Test
    void keywordRepositorySave_multipleKeywords_differentIds() {
        var keyword0 = keywordRepository.save(new Keyword("keyword_name0"));
        var keyword1 = keywordRepository.save(new Keyword("keyword_name1"));

        assertNotEquals(keyword0.getID(), keyword1.getID());
    }
}
