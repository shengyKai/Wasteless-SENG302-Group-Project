package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.randomInstant;

public class BoughtSaleItemGenerator {
    private final Random random = new Random();
    private final Connection conn;
    private final Logger logger = LogManager.getLogger(BoughtSaleItemGenerator.class.getName());

    public BoughtSaleItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Create the SQL statement to insert the bought sale item into the database.
     * @param productId The ID number of the product to associate with this sale item.
     * @param buyerId The ID number of the user who is the buyer of this sale item.
     * @return The ID number of the sale item.
     */
    private long createInsertBoughtSaleItemSQL(long productId, long buyerId) throws SQLException {
        var likeCount = random.nextInt(500);
        var listingDate = randomInstant(Instant.parse("2021-01-01T00:00:00Z"), Instant.now());
        var price = (float) random.nextInt(100000) / 100;
        var quantity = random.nextInt(100);
        var saleDate = randomInstant(listingDate, Instant.now());


        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bought_sale_item(like_count, listing_date, price, quantity, sale_date, buyer, product_id)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, likeCount);
            stmt.setObject(2, listingDate);
            stmt.setObject(3, price);
            stmt.setObject(4, quantity);
            stmt.setObject(5, saleDate);
            stmt.setObject(6, buyerId);
            stmt.setObject(7, productId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    /**
     * Generates the bought sale items.
     * @param productIds List of all the IDs of products to associate with bought sale items.
     * @param userIds List of all the IDs of users to be buyers of the bought sale items.
     * @param boughtSaleItemCount Number of sale items to generate.
     * @return List of ID number for the generated sale items.
     */
    public List<Long> generateBoughtSaleItems(List<Long> productIds, List<Long> userIds, int boughtSaleItemCount) throws SQLException {
        List<Long> boughtSaleItemIds = new ArrayList<>();
        for (int i = 0; i < boughtSaleItemCount; i++) {
            logger.info("Creating Bought Sale Item {} / {}", i+1, boughtSaleItemCount);
            int progress = (int) (((float)(i+1) / (float)boughtSaleItemCount) * 100);
            logger.info("Progress: {}%", progress);

            long productId = productIds.get(random.nextInt(productIds.size()));
            long buyerId = userIds.get(random.nextInt(userIds.size()));
            long boughtSaleItemId = createInsertBoughtSaleItemSQL(productId, buyerId);

            boughtSaleItemIds.add(boughtSaleItemId);
        }
        return boughtSaleItemIds;
    }
}
