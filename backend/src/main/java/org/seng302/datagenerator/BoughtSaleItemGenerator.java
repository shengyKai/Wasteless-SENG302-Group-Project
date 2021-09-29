package org.seng302.datagenerator;

import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.randomInstant;

public class BoughtSaleItemGenerator {
    private Random random = new Random();
    private Connection conn;
    private final Logger logger = LogManager.getLogger(BoughtSaleItemGenerator.class.getName());

    public BoughtSaleItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Object representing the dates associated with a bought sale item
     */
    @Value
    private static class SaleDates {
        Instant listingDate;
        Instant saleDate;
    }

    private Instant getUserCreationDate(long userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT created FROM user WHERE id = ?"
        )) {
            stmt.setObject(1, userId);
            stmt.executeQuery();
            ResultSet results = stmt.getResultSet();
            results.next();

            return results.getObject("created", OffsetDateTime.class).toInstant();
        }
    }

    private Instant getProductCreationDate(long productId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT created FROM product WHERE id = ?"
        )) {
            stmt.setObject(1, productId);
            stmt.executeQuery();
            ResultSet results = stmt.getResultSet();
            results.next();

            return results.getObject("created", OffsetDateTime.class).toInstant();
        }
    }

    private SaleDates generateSaleDates(long productId, long buyerId) throws SQLException {
        var productCreationDate = getProductCreationDate(productId);
        var buyerCreationDate = getUserCreationDate(buyerId);
        var minListingDate = Collections.max(List.of(productCreationDate, buyerCreationDate));
        var listingDate = randomInstant(minListingDate, Instant.now());
        var saleDate = randomInstant(listingDate, Instant.now());
        return new SaleDates(listingDate, saleDate);
    }


    private long createInsertBoughtSaleItemSQL(long productId, long buyerId) throws SQLException {
        var likeCount = random.nextInt(500);
        var price = (float) random.nextInt(100000) / 100;
        var quantity = random.nextInt(100);
        var saleDates = generateSaleDates(productId, buyerId);


        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bought_sale_item(like_count, listing_date, price, quantity, sale_date, buyer, product_id)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, likeCount);
            stmt.setObject(2, saleDates.getListingDate());
            stmt.setObject(3, price);
            stmt.setObject(4, quantity);
            stmt.setObject(5, saleDates.getSaleDate());
            stmt.setObject(6, buyerId);
            stmt.setObject(7, productId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

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
