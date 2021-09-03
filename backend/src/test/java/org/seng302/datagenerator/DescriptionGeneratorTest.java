package org.seng302.datagenerator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.leftovers.entities.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes=DescriptionGenerator.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DescriptionGeneratorTest {
    DescriptionGenerator descGen = DescriptionGenerator.getInstance();
    String description;

    User testUser;
    MarketplaceCard card;
    Business testBusiness;
    Product product;

    @BeforeAll
    void setup() {
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
        var closes = Instant.now().plus(1000, ChronoUnit.HOURS);
        card = new MarketplaceCard.Builder()
                .withCreator(testUser)
                .withSection(MarketplaceCard.Section.EXCHANGE)
                .withTitle("test_title")
                .withDescription("test_description")
                .withCloses(closes)
                .build();
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser)
                .build();
        product = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness)
                .build();
    }

    @Test
    void randomDescriptionTest() {
        description = descGen.randomDescription();
        assertTrue(description.length() < 200);
        assertTrue(description.length() >= 10);
    }

    @Test
    void marketplaceCardDescValid() {
        description = descGen.randomDescription();
        assertDoesNotThrow(()->card.setDescription(description));
    }

    @Test
    void userBioValid() {
        description = descGen.randomDescription();
        assertDoesNotThrow(()->testUser.setBio(description));
    }

    @Test
    void businessDescValid() {
        description = descGen.randomDescription();
        assertDoesNotThrow(()->testBusiness.setDescription(description));
    }

    @Test
    void productDescValid() {
        description = descGen.randomDescription();
        assertDoesNotThrow(()->product.setDescription(description));
    }
}
