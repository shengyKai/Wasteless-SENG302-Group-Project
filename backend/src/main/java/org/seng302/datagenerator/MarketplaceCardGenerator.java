package org.seng302.datagenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.*;

public class MarketplaceCardGenerator {
    private Random random = new Random();
    private Connection conn;

    /**
     * Constructor for Marketplace Card Generator, establishes connection to db
     * @param conn to db
     */
    public MarketplaceCardGenerator(Connection conn) { this.conn = conn; }

    /**
     * SQL statement to insert created card into database
     * @return id of created card
     */
    private long createInsertCardSQL() {
        return -1;
    }

    /**
     * Generate a certain amount of marketplace cards and insert them into the database
     * @param userIds users that post cards on the marketplace
     * @param cardCount amount of example cards to create
     */
    private void generateCards(List<Long> userIds, int cardCount) {

    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var userGenerator = new UserGenerator(conn);
        var marketplaceCardGenerator = new MarketplaceCardGenerator(conn);

        int userCount = getNumObjectsFromInput("users");
        List<Long> userIds = userGenerator.generateUsers(userCount);

        int cardCount = getNumObjectsFromInput("marketplace cards");
        marketplaceCardGenerator.generateCards(userIds, cardCount);
    }
}
