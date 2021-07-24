package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchMarketplaceCardHelperTest {

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeAll
    void initialise() {
        marketplaceCardRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        testUser = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Cory")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        marketplaceCardRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Generates a card with the provided keywords
     * @param keywords Keywords for the card to have
     * @return Generated card
     */
    private MarketplaceCard createCard(Keyword... keywords) {
        return createCard(Arrays.asList(keywords));
    }

    /**
     * Generates a card with the provided keywords
     * @param keywords List of keywords for the card to have
     * @return Generated card
     */
    private MarketplaceCard createCard(List<Keyword> keywords) {
        var card = new MarketplaceCard.Builder()
                    .withCreator(testUser)
                    .withSection(MarketplaceCard.Section.EXCHANGE)
                    .withTitle("This is the title")
                    .withDescription("Description")
                    .build();

        for (Keyword keyword : keywords)
            card.addKeyword(keyword);

        return marketplaceCardRepository.save(card);
    }

    @Test
    void cardHasKeywords_orSelection_returnsCardsWithAnyOfTheKeywords() {
        Keyword keyword1 = keywordRepository.save(new Keyword("This"));
        Keyword keyword2 = keywordRepository.save(new Keyword("That"));
        keywordRepository.save(new Keyword("Other"));

        MarketplaceCard card1 = createCard(keyword1, keyword2);
        MarketplaceCard card2 = createCard(keyword2);

        Specification<MarketplaceCard> spec = SearchMarketplaceCardHelper.cardHasKeywords(List.of(keyword1, keyword2), true);

        List<MarketplaceCard> cards = marketplaceCardRepository.findAll(spec);
        Set<Long> cardIds = cards.stream().map(MarketplaceCard::getID).collect(Collectors.toSet());

        assertEquals(Set.of(card1.getID(), card2.getID()), cardIds);
    }

    @Test
    void cardHasKeywords_andSelection_returnsCardsWithAllOfTheKeywords() {
        Keyword keyword1 = keywordRepository.save(new Keyword("This"));
        Keyword keyword2 = keywordRepository.save(new Keyword("That"));
        keywordRepository.save(new Keyword("Other"));

        MarketplaceCard card1 = createCard(keyword1, keyword2);
        MarketplaceCard card2 = createCard(keyword2);

        Specification<MarketplaceCard> spec = SearchMarketplaceCardHelper.cardHasKeywords(List.of(keyword1, keyword2), false);

        List<MarketplaceCard> cards = marketplaceCardRepository.findAll(spec);
        Set<Long> cardIds = cards.stream().map(MarketplaceCard::getID).collect(Collectors.toSet());

        assertEquals(Set.of(card1.getID()), cardIds);
    }
}
