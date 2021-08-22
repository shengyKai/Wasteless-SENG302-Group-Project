package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.randomDate;

public class InventoryItemGenerator {
    private Random random = new Random();
    private Connection conn;
    private final Logger logger = LogManager.getLogger(InventoryItemGenerator.class.getName());

    public InventoryItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Randomly generates the best before date, creation date, expires date, manufactured date, and the sell by date
     * @return a list containing the five dates
     */
    private String[] generateDates () {
        //creation < manufactured < today < sell by < best before < expires
        LocalDate today = LocalDate.now();
        LocalDate minimumDate = today.minusYears(2);
        LocalDate maximumDate = today.plusYears(2);

        LocalDate creation = randomDate(minimumDate, today);
        LocalDate manufactured = randomDate(creation, today);
        LocalDate sellBy = randomDate(today, maximumDate);
        LocalDate bestBefore = randomDate(sellBy, maximumDate);
        LocalDate expires = randomDate(bestBefore, maximumDate);

        return new String[] {creation.toString(), manufactured.toString(), sellBy.toString(), bestBefore.toString(), expires.toString()};
    }

    /**
     * Randomly generates the price per item
     * @return the price per item
     */
    private float generatePricePerItem() {
        int pricex100 = random.nextInt(100000);
        return ((float) pricex100) / 100;
    }

    /**
     * Randomly generates the quantity and remaining quantity of inventory items
     * @return the quantity
     */
    private int[] generateQuantities() {
        int quantity = random.nextInt(249) + 1;
        int remainingQuantity = random.nextInt(quantity);
        return new int[]{quantity, remainingQuantity};
    }

    /**
     * Randomly generates the version of the product
     * @return the product version
     */
    private int generateVersion() { return random.nextInt(10); }

    /**
     * Creates and inserts the product into the database
     * @param productId the id associated with the product entity representing what product the inventory item is
     * @return the id of inventory item
     */
    private long createInsertInventoryItemSQL(long productId) throws SQLException {
        String[] dates = generateDates();
        String bestBefore = dates[0];
        String creationDate = dates[1];
        String expires = dates[2];
        String manufactured = dates[3];
        String sellBy = dates[4];

        int[] quantities = generateQuantities();
        int quantity = quantities[0];
        int remainingQuantity = quantities[1];

        float pricePerItem = generatePricePerItem();

        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO inventory_item(best_before, creation_date, expires, manufactured, price_per_item, quantity, " +
                    "remaining_quantity, sell_by, total_price, version, product_id)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, bestBefore); //best before date
            stmt.setObject(2, creationDate); //creation date
            stmt.setObject(3, expires); //expiration date
            stmt.setObject(4, manufactured); //manufactured date
            stmt.setObject(5, pricePerItem); //price per item
            stmt.setObject(6, quantity); //quantity
            stmt.setObject(7, remainingQuantity); //remaining quantity
            stmt.setObject(8, sellBy); //sell by date
            stmt.setObject(9, remainingQuantity * pricePerItem); //total price
            stmt.setObject(10, generateVersion()); //version of product
            stmt.setObject(11, productId); //product id
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    /**
     * Generates the inventory items
     * @param invItemCount
     * @return
     */
    public List<Long> generateInventoryItems(List<Long> productIds, int invItemCount) throws SQLException {
        List<Long> generatedInvItemIds = new ArrayList<>();
        for (int i=0; i < invItemCount; i++) {
            long productId = productIds.get(random.nextInt(productIds.size()));

            logger.info("Creating Inventory Item {} / {}", i+1, invItemCount);
            int progress = (int) (((float)(i+1) / (float)invItemCount) * 100);
            logger.info("Progress: {}%", progress);
            long invItemId = createInsertInventoryItemSQL(productId);

            generatedInvItemIds.add(invItemId);
        }

        return generatedInvItemIds;
    }
}
