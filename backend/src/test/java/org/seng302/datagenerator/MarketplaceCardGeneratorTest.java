package org.seng302.datagenerator;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class MarketplaceCardGeneratorTest {
    private Connection conn;
    private MarketplaceCardGenerator marketplaceCardGenerator;
    private List<Long> userIds;

    private static final List<String> DEFAULT_KEYWORD_NAMES = List.of("Free", "Home Baking", "With Love", "Inspired", "Bulk", "Gluten Free", "Vegan", "Vegetarian", "Fun", "Keto", "Plant Based", "Fat Free", "Italian", "French", "Local Produce", "Fresh", "Eco", "Sustainable", "Asian", "Curry", "Dairy", "Perishable", "Non Perishable", "Free Range", "Natural", "Organic", "Connor", "Kosher", "Paleo", "Home Grown");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn = DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        marketplaceCardGenerator = new MarketplaceCardGenerator(conn);
        UserGenerator userGenerator = new UserGenerator(conn);
        userIds = userGenerator.generateUsers(10);
    }

    @AfterEach
    public void teardown() throws SQLException {
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks card was saved to db and all fields are set
     * @param cardId of generated card
     * @return whether the card is successful or not
     */
    private boolean checkFields(Long cardId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM marketplace_card WHERE id = ? AND creator_id IS NOT NULL " +
                "AND created IS NOT NULL AND section IS NOT NULL AND title IS NOT NULL and description IS NOT NULL " +
                "AND closes IS NOT NULL");
        stmt.setObject(1, cardId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1) == 1;
    }

    /**
     * Check the keywords were set in db
     * @param cardId of generated card
     * @return whether keywords were successful or not
     */
    private boolean checkKeywords(Long cardId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM card_keywords WHERE cards_id = ?");
        stmt.setObject(1, cardId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        // Generator sets 1-6 keywords to each card,
        // have to assume if one saved they all did as no way to know how many were generated
        return results.getLong(1) > 0 && results.getLong(1) <= 6;
    }

    @Test
    void generateCards_SingleCard_OneCardGenerated() throws SQLException {
        List<Long> cardIds = marketplaceCardGenerator.generateCards(userIds,1);
        if (cardIds.size() != 1) fail();
        assertTrue(checkFields(cardIds.get(0)));
        assertTrue(checkKeywords(cardIds.get(0)));
    }

    @Test
    void generateCards_ManyCard_ManyCardsGenerated() throws SQLException {
        List<Long> cardIds = marketplaceCardGenerator.generateCards(userIds,100);
        if (cardIds.size() != 100) fail();
        for (Long cardId : cardIds) {
            assertTrue(checkFields(cardId));
            assertTrue(checkKeywords(cardId));
        }
    }

    @Test
    void generateCards_ZeroCard_NoCardsGenerated() throws SQLException {
        List<Long> cardIds = marketplaceCardGenerator.generateCards(userIds,0);
        if (cardIds.size() != 0) fail();
    }

    @Test
    void generateCards_NegativeCard_NoCardsGenerated() throws SQLException {
        List<Long> cardIds = marketplaceCardGenerator.generateCards(userIds,-10);
        if (cardIds.size() != 0) fail();
    }
}
