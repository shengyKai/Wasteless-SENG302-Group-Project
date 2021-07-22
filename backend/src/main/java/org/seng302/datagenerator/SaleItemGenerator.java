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

        return new String[] {closes.toString(), created.toString()};
    }

    /**
     * Randomly generates the quantity for sale item and remaining quantity of inventory item
     * @return the quantity values in a list
     */
    private int[] generateQuantities(int upperLimit) {
        int quantity = random.nextInt(upperLimit);
        int remainingQuantity = upperLimit - quantity;
        return new int[]{quantity, remainingQuantity};
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
     * Creates and inserts the product into the database
     * @param productId the id associated with the product entity representing what product the inventory item is
     * @return the id of inventory item
     */
    private long createInsertSaleItemSQL(long invItemId) throws SQLException {
        String[] invItemInfo = extractInvItemInfo(invItemId);
        String[] dates = generateDates(invItemInfo[0], invItemInfo[1]);

        String closes = dates[0];
        String created = dates[1];

        String moreInfo = DescriptionGenerator.getInstance().randomDescription();

        float price = generatePricePerItem();

        int[] quantities = generateQuantities(Integer.parseInt(invItemInfo[2]));
        int quantity = quantities[0];
        int remainingQuantity = quantities[1];

        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO sale_item(closes, created, more_info, price, quantity, inventory_item_id)"
                + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, closes); 
        stmt.setObject(2, created);
        stmt.setObject(3, moreInfo);
        stmt.setObject(4, price);
        stmt.setObject(5, quantity);
        stmt.setObject(6, invItemId);
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
            long invItemId = invItemIds.get(i);

            System.out.println(String.format("Creating Sale Item %d / %d", i+1, saleItemCount));
            int progress = (int) (((float)(i+1) / (float)saleItemCount) * 100);
            System.out.println(String.format("Progress: %d%%", progress));
            long saleItemId = createInsertSaleItemSQL(invItemId);

            generatedSaleItemIds.add(invItemId);
        }
        return generatedSaleItemIds;
    }
}
