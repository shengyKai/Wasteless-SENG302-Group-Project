package org.seng302.datagenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.*;

public class SaleItemGenerator {
    private Random random = new Random();
    private Connection conn;

    public SaleItemGenerator(Connection conn) { this.conn = conn; }

    private String[] extractInvItemInfo(long invItemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT expires, creation_date, quantity FROM inventory_item WHERE id = ?"
        );
        stmt.setObject(1, invItemId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return new String[] {results.getString("expires"), results.getString("creation_date"), results.getString("quantity")};
    }

    /**
     * Randomly generates the closes, created date
     * @return a list containing the two dates
     */
    private String[] generateDates(String expires, String creationDate) {
        LocalDate parsedExpires = LocalDate.parse(expires);
        LocalDate parsedCreationDate = LocalDate.parse(creationDate);
        LocalDate today = LocalDate.now();

        LocalDate created = randomDate(parsedCreationDate, today);
        LocalDate closes = randomDate(created, parsedExpires);

        return new String[] {created.toString(), closes.toString()};
    }

    /**
     * Creates and inserts the product into the database
     * @param productId the id associated with the product entity representing what product the inventory item is
     * @return the id of inventory item
     */
    private long createInsertSaleItemSQL(long invItemId) throws SQLException {
        String[] invItemInfo = extractInvItemInfo(invItemId);
        String[] dates = generateDates(invItemInfo[0], invItemInfo[1]);
        
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
            "INSERT INTO sale_item(closes, created, more_info, price, quantity, inventory_item_id)"
                + "VALUES (?, ?, ?, ?, ?, ?)",
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
        var userGenerator = new UserGenerator(conn);
        var businessGenerator = new BusinessGenerator(conn);
        var productGenerator = new ProductGenerator(conn);
        var invItemGenerator = new InventoryItemGenerator(conn);
        var saleItemGenerator = new SaleItemGenerator(conn);

        int userCount = getNumObjectsFromInput("users");
        List<Long> userIds = userGenerator.generateUsers(userCount);

        int businessCount = getNumObjectsFromInput("businesses");
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);

        int productCount = getNumObjectsFromInput("products");
        List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);

        int invItemCount = getNumObjectsFromInput("inventory items");
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, invItemCount);

        int saleItemCount = getNumObjectsFromInput("sale items");
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, saleItemCount);
    }

    /**
     * Generates the sale items
     * @param saleItemCount count of the number of sale items to be generated
     * @return
     */
    public List<Long> generateSaleItems(List<Long> invItemIds, int saleItemCount) throws SQLException {
        List<Long> generatedSaleItemIds = new ArrayList<>();
        for (int i=0; i < saleItemCount; i++) {
            clear();
            long invItemId = invItemIds.get(0);

            System.out.println(String.format("Creating Inventory Item %d / %d", i+1, saleItemCount));
            int progress = (int) (((float)(i+1) / (float)saleItemCount) * 100);
            System.out.println(String.format("Progress: %d%%", progress));
            long saleItemId = createInsertSaleItemSQL(invItemId);

            generatedSaleItemIds.add(invItemId);
        }
        return generatedSaleItemIds;
    }
}
