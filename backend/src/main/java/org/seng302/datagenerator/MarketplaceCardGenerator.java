package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.*;

public class MarketplaceCardGenerator {
    private final Random random = new Random();
    private final Connection conn;
    private final DescriptionGenerator descGen;

    private static final String CARD_TITLES_FILE = "card-titles.txt";
    private final List<String> cardTitles;
    private final Logger logger = LogManager.getLogger(MarketplaceCardGenerator.class.getName());

    /**
     * Constructor for Marketplace Card Generator, establishes connection to db
     * @param conn to db
     */
    public MarketplaceCardGenerator(Connection conn) {
        this.conn = conn;
        cardTitles = ExampleDataFileReader.readExampleDataFile(CARD_TITLES_FILE);
        descGen = DescriptionGenerator.getInstance();
    }

    /**
     * SQL statement to insert created card into database
     * @param userIds list of user ids to be card creators
     * @return id of created card
     * @throws SQLException
     */
    private long createInsertCardSQL(List<Long> userIds) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate created = randomDate(today.minusDays(5), today);

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO marketplace_card (creator_id, section, title, description, created, closes, last_renewed) " +
                        "VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, userIds.get(random.nextInt(userIds.size())));
            stmt.setObject(2, random.nextInt(3));
            stmt.setObject(3, cardTitles.get(random.nextInt(cardTitles.size())));
            stmt.setObject(4, descGen.randomDescription());
            stmt.setObject(5, created);
            stmt.setObject(6, created.plus(14, ChronoUnit.DAYS));
            stmt.setObject(7, created);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    /**
     * Gives each card associated keywords
     * @param cardId card keyword is to be associated with
     * @param keywords list of keyword ids card is to be associated with
     * @throws SQLException
     */
    private void cardKeywordSQL(Long cardId, List<Long> keywords) throws SQLException {
        for (Long keyword : keywords) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO card_keywords (cards_id, keywords_id) " +
                    "VALUES (?,?)")) {
                stmt.setObject(1, cardId);
                stmt.setObject(2, keyword);
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Get all current keywords in the db
     * @return list of keyword ids
     * @throws SQLException
     */
    private List<Long> loadKeywords() throws SQLException {
        List<Long> keywords = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM keyword")) {
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                keywords.add(result.getLong(1));
            }
            return keywords;
        }
    }

    /**
     * Generate a certain amount of marketplace cards and insert them into the database
     * @param userIds users that post cards on the marketplace
     * @param cardCount amount of example cards to create
     * @throws SQLException
     */
    public List<Long> generateCards(List<Long> userIds, int cardCount) throws SQLException {
        List<Long> generatedCardIds = new ArrayList<>();
        List<Long> keywordIds = loadKeywords();
        try {
            logger.info("Generating {} marketplace cards", cardCount);
            for (int i = 0; i < cardCount; i++) {
                clear();

                if (i % 10 == 0) {
                    int progress = (int) (((float) (i + 1) / (float) cardCount) * 100);
                    logger.info("Progress: {}%", progress);
                }

                long cardId = createInsertCardSQL(userIds);
                generatedCardIds.add(cardId);

                List<Long> keywords = new ArrayList<>();
                int numKeys = random.nextInt(6)+1;
                for (int j=0; j<numKeys; j++) {
                    long keyword = keywordIds.get(random.nextInt(keywordIds.size()));
                    if (!keywords.contains(keyword)) keywords.add(keyword);
                }
                cardKeywordSQL(cardId, keywords);
                keywords.clear();
            }
            return generatedCardIds;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return List.of();
        }
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException {
        Connection conn = connectToDatabase();
        var userGenerator = new UserGenerator(conn);
        var marketplaceCardGenerator = new MarketplaceCardGenerator(conn);

        int userCount = getNumObjectsFromInput("users");
        List<Long> userIds = userGenerator.generateUsers(userCount);

        int cardCount = getNumObjectsFromInput("marketplace cards");
        marketplaceCardGenerator.generateCards(userIds, cardCount);
    }
}
