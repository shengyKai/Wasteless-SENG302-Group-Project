package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

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
        marketplaceCardRepository.save(card);
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
        System.out.println(cards);
        System.out.println(card);
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

        List<MarketplaceCard> results = marketplaceCardRepository.getAllExpiringBefore(cutoff);
        Assertions.assertEquals(shouldReturnCard, results.contains(card));
    }

    @ParameterizedTest
    @MethodSource("closesAndCutoff")
    void getAllExpiringBefore_expiryEventExists_cardNotReturned(Instant closes, Instant cutoff) {
        card.setCloses(closes);
        card = marketplaceCardRepository.save(card);

        ExpiryEvent event = new ExpiryEvent(card);
        eventService.addUserToEvent(card.getCreator(), event);
        Assertions.assertTrue(expiryEventRepository.getByExpiringCard(card).isPresent());

        List<MarketplaceCard> results = marketplaceCardRepository.getAllExpiringBefore(cutoff);
        Assertions.assertFalse(results.contains(card));
    }



}
