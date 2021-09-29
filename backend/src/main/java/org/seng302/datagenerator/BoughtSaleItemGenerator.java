package org.seng302.datagenerator;

import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Instant saleDate;
        Instant listingDate;
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
        throw new RuntimeException("Not yet implemented");
    }


    private long createInsertBoughtSaleItemSQL(long productId, long buyerId) throws SQLException {
        throw new RuntimeException("Not yet implemented");
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
