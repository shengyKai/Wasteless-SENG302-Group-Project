package org.seng302.datagenerator;

import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.randomDate;
import static org.seng302.datagenerator.Main.randomInstant;

public class SaleItemGenerator {
    private Random random = new Random();
    private Connection conn;
    private final Logger logger = LogManager.getLogger(SaleItemGenerator.class.getName());

    public SaleItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Object representing some of the fields of an inventory item that a required form making sale items
     */
    @Value
    private static class InventoryItemInfo {
        LocalDate expires;
        Instant creation;
        int quantity;
        int remainingQuantity;
    }

    /**
     * Extracts the essential information from inventory item table so that the newly generated sale items can be
     * constructed within the constraints of the associated inventory item attributes
     * @param invItemId id of the inventory item to get information from
     * @return A record of the relevant inventory item info
     * @throws SQLException
     */
    private InventoryItemInfo extractInvItemInfo(long invItemId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT expires, creation_date, remaining_quantity, quantity FROM inventory_item WHERE id = ?"
        )) {
            stmt.setObject(1, invItemId);
            stmt.executeQuery();
            ResultSet results = stmt.getResultSet();
            results.next();

            return new InventoryItemInfo(
                    results.getObject("expires", LocalDate.class),
                    results.getObject("creation_date", OffsetDateTime.class).toInstant(),
                    results.getInt("quantity"),
                    results.getInt("remaining_quantity")
            );
        }
    }

    /**
     * Object representing the dates associated with a listing
     */
    @Value
    private static class ListingDates {
        Instant created;
        LocalDate closes;
    }

    /**
     * Randomly generates the closes, created date for a sale listing
     * @return A record of the sale item dates
     */
    private ListingDates generateListingDates(LocalDate expires, Instant creationDate) {
        Instant created = randomInstant(creationDate, Instant.now());
        LocalDate createdDate = LocalDateTime.ofInstant(created, Clock.systemDefaultZone().getZone()).toLocalDate();

        LocalDate closes = randomDate(createdDate, expires);

        return new ListingDates(created, closes);
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
     * Updates the associated inventory item quantity with a new quantity.
     * @param quantity the new quantity from generating the sale item
     * @param invItemId the associated inventory item id of the generated sale item
     * @throws SQLException
     */
    private void updateInventoryItemQuantity(int quantity, long invItemId) throws SQLException {
        logger.info("Updating inventory item quantity of id {}", invItemId);
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE inventory_item SET quantity = ? WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, quantity);
            stmt.setObject(2, invItemId);
            stmt.executeUpdate();
        }
    }

    /**
     * Creates and inserts the sale item into the database
     * @param inventoryItemId the id associated with the inventory item entity
     * @param inventoryItemInfo information of the inventory item to create sales item with
     * @return the id of sale item
     */
    private long createInsertSaleItemSQL(long inventoryItemId, InventoryItemInfo inventoryItemInfo) throws SQLException {
        ListingDates dates = generateListingDates(inventoryItemInfo.getExpires(), inventoryItemInfo.getCreation());

        String moreInfo = DescriptionGenerator.getInstance().randomDescription();

        float price = generatePricePerItem();

        int quantity = random.nextInt(inventoryItemInfo.getRemainingQuantity());
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO sale_item(closes, created, more_info, price, quantity, inventory_item_id)"
                + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, dates.getCloses());
            stmt.setObject(2, dates.getCreated());
            stmt.setObject(3, moreInfo);
            stmt.setObject(4, price);
            stmt.setObject(5, quantity);
            stmt.setObject(6, inventoryItemId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            updateInventoryItemQuantity(inventoryItemInfo.getQuantity() + quantity, inventoryItemId);
            return keys.getLong(1);
        }
    }

    /**
     * Generates the sale items
     * @param saleItemCount count of the number of sale items to be generated
     * @param invItemIds List of all the ids of the inventory items
     * @return
     */
    public List<Long> generateSaleItems(List<Long> invItemIds, int saleItemCount) throws SQLException {
        List<Long> generatedSaleItemIds = new ArrayList<>();
        for (int i=0; i < saleItemCount; i++) {
            logger.info("Creating Sale Item {} / {}", i+1, saleItemCount);
            int progress = (int) (((float)(i+1) / (float)saleItemCount) * 100);
            logger.info("Progress: {}%", progress);

            //Inventory items can have multiple sales, as such, if we randomize the which inventory item is chosen
            //we can allow some sale items to exist for the same inventory item.
            long invItemId = invItemIds.get(random.nextInt(invItemIds.size()));
            InventoryItemInfo invItemInfo = extractInvItemInfo(invItemId);
            long saleItemId = createInsertSaleItemSQL(invItemId, invItemInfo);

            generatedSaleItemIds.add(saleItemId);
        }
        return generatedSaleItemIds;
    }
}
