package org.seng302.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketplaceCardTests {

    @Autowired
    MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    UserRepository userRepository;

    User testUser;

    @BeforeEach
    void setUp() throws Exception{
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();

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
}
