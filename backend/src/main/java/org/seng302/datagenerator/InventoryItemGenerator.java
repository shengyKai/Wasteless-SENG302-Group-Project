package org.seng302.datagenerator;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.*;

public class InventoryItemGenerator {
    private Random random = new Random();
    private Connection conn;

    public InventoryItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Randomly generates the best before date, creation date, expires date, manufactured date, and the sell by date
     * @return a list containing the five dates
     */
    private String[] generateDates () {
        //TODO
        return null;
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
        int quantity = random.nextInt(250);
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

        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO inventory_item(best_before, creation_date, expires, manufactured, price_per_item, quantity, " +
                    "remaining_quantity, sell_by, total_price, version, product_id)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
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

    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new InventoryItemGenerator(conn);

        int invItemCount = getNumObjectsFromInput("inventory items");
        generator.generateInventoryItems(invItemCount);
    }

    /**
     * Generates the inventory items
     * @param invItemCount
     * @return
     */
    private List<Long> generateInventoryItems(int invItemCount) throws SQLException {
        var productGenerator = new ProductGenerator(conn);
        List<Long> generatedInvItemIds = new ArrayList<>();
        try {
            for (int i=0; i < invItemCount; i++) {
                clear();
                List<Long> productIds = productGenerator.generateProducts(1);
                long productId = productIds.get(0);

                System.out.println(String.format("Creating Inventory Item %d / %d", i+1, invItemCount));
                int progress = (int) (((float)(i+1) / (float)invItemCount) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long invItemId = createInsertInventoryItemSQL(productId);

                generatedInvItemIds.add(invItemId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return generatedInvItemIds;
    }
}
