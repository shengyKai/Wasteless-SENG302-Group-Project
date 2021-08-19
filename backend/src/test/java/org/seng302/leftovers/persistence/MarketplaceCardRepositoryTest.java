package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.service.EventService;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.error.Mark;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketplaceCardRepositoryTest {
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExpiryEventRepository expiryEventRepository;
    @Autowired
    private EventService eventService;
    private MarketplaceCard card;
    private User user;

    @BeforeEach
    private void setUp() {
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        Location address = new Location.Builder()
                .inCity("city")
                .inCountry("New Zealand")
                .inRegion("region")
                .onStreet("street")
                .atStreetNumber("3")
                .withPostCode("222")
                .build();
        user = new User.Builder()
                .withEmail("john@smith.com")
                .withFirstName("John")
                .withLastName("Smith")
                .withAddress(address)
                .withPassword("password123")
                .withDob("2000-08-04")
                .build();
        user = userRepository.save(user);
        card = new MarketplaceCard.Builder()
                .withTitle("Some Title")
                .withDescription("Some description")
                .withCreator(user)
                .withSection("Wanted")
                .build();
        card = marketplaceCardRepository.save(card);
    }

    @AfterEach
    private void tearDown() {
        expiryEventRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Returns a stream of section names
     * @return Section names
     */
    private static Stream<Arguments> sections() {
        return Stream.of(
                Arguments.of("Wanted"),
                Arguments.of("ForSale"),
                Arguments.of("Exchange")
        );
    }

    /**
     * Maps params for a cards section to a search section term
     */
    private static Stream<Arguments> invalidSections() {
        return Stream.of(
                Arguments.of("Wanted", "ForSale"),
                Arguments.of("ForSale", "Exchange"),
                Arguments.of("Exchange", "Wanted")
        );
    }

    @ParameterizedTest
    @MethodSource("sections")
    void getAllBySection_getsCardsFromCorrectSection(String sectionName) {
        MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionName);
        card.setSection(section);
        card = marketplaceCardRepository.save(card);

        List<MarketplaceCard> cards = marketplaceCardRepository.getAllBySection(section);
        Assertions.assertTrue(cards.contains(card));

    }

    @ParameterizedTest
    @MethodSource("invalidSections")
    void getAllBySection_doesntGetCardsFromOtherSections(String cardSectionName, String searchSectionName) {
        MarketplaceCard.Section cardSection = MarketplaceCard.sectionFromString(cardSectionName);
        card.setSection(cardSection);
        card = marketplaceCardRepository.save(card);

        MarketplaceCard.Section searchSection = MarketplaceCard.sectionFromString(searchSectionName);

        List<MarketplaceCard> cards = marketplaceCardRepository.getAllBySection(searchSection);
        Assertions.assertFalse(cards.contains(card));

    }

    private Stream<Arguments> closesAndCutoff() {
        return Stream.of(
                Arguments.of(Instant.now().plus(Duration.ofDays(2)), Instant.now().plus(Duration.ofDays(1))),
                Arguments.of(Instant.now().plus(Duration.ofDays(1)), Instant.now().plus(Duration.ofDays(2))));
    }

    @ParameterizedTest
    @MethodSource("closesAndCutoff")
    void getAllExpiringBefore_noExpiryEvent_cardReturnedIfClosesBeforeCutoff(Instant closes, Instant cutoff) {
        boolean shouldReturnCard = closes.isBefore(cutoff);

        card.setCloses(closes);
        card = marketplaceCardRepository.save(card);

        Assertions.assertTrue(expiryEventRepository.getByExpiringCard(card).isEmpty());

        List<MarketplaceCard> results = marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(cutoff);
        assertEquals(shouldReturnCard, results.contains(card));
    }

    @ParameterizedTest
    @MethodSource("closesAndCutoff")
    void getAllExpiringBefore_expiryEventExists_cardNotReturned(Instant closes, Instant cutoff) {
        card.setCloses(closes);
        card = marketplaceCardRepository.save(card);

        ExpiryEvent event = new ExpiryEvent(card);
        eventService.saveEvent(event);
        Assertions.assertTrue(expiryEventRepository.getByExpiringCard(card).isPresent());

        List<MarketplaceCard> results = marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(cutoff);
        Assertions.assertFalse(results.contains(card));
    }

    @Test
    void getAllByCreator_sortByCreated_returnsValidOrdering() throws Exception {
        // Remove existing card that will interfere with results
        marketplaceCardRepository.delete(card);

        Field created = MarketplaceCard.class.getDeclaredField("created");
        created.setAccessible(true);

        Instant now = Instant.now();

        MarketplaceCard card1 = new MarketplaceCard.Builder()
                .withCreator(user)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("This")
                .withDescription("That")
                .build();
        created.set(card1, now.minus(1, ChronoUnit.HOURS));
        card1 = marketplaceCardRepository.save(card1);

        MarketplaceCard card2 = new MarketplaceCard.Builder()
                .withCreator(user)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("This")
                .withDescription("That")
                .build();
        created.set(card2, now.minus(3, ChronoUnit.HOURS));
        card2 = marketplaceCardRepository.save(card2);

        MarketplaceCard card3 = new MarketplaceCard.Builder()
                .withCreator(user)
                .withSection(MarketplaceCard.Section.WANTED)
                .withTitle("This")
                .withDescription("That")
                .build();
        created.set(card3, now.minus(2, ChronoUnit.HOURS));
        card3 = marketplaceCardRepository.save(card3);

        // Same page request as in GET /users/:id/cards
        var pageRequest = SearchHelper.getPageRequest(null, null, Sort.by(new Sort.Order(Sort.Direction.DESC, "created")));
        Page<MarketplaceCard> result = marketplaceCardRepository.getAllByCreator(user, pageRequest);

        List<MarketplaceCard> expectedOrder = List.of(card1, card3, card2);
        assertEquals(expectedOrder, result.getContent());

        assertEquals(3, result.getTotalElements());
    }
}
