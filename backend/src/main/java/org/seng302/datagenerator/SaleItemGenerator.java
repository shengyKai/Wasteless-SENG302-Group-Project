package org.seng302.datagenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import net.minidev.json.JSONObject;

import static org.seng302.datagenerator.Main.*;

public class SaleItemGenerator {
    private Random random = new Random();
    private Connection conn;

    public SaleItemGenerator(Connection conn) { this.conn = conn; }

    /**
     * Extracts the essential information from inventory item table so that the newly generated sale items can be
     * constructed within the constraints of the associated inventory item attributes
     * @param invItemId id of the inventory item to get information from
     * @return a string list of the information of the inventory item
     * @throws SQLException
     */
    private String[] extractInvItemInfo(long invItemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT expires, creation_date, remaining_quantity FROM inventory_item WHERE id = ?"
        );
        stmt.setObject(1, invItemId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return new String[] {results.getString("expires"), results.getString("creation_date"), results.getString("remaining_quantity")};
    }

    /**
     * Randomly generates the closes, created date
     * @return a list containing the two dates
     */
    private String[] generateDates(String expires, String creationDate) {
        LocalDate parsedExpires = LocalDate.parse(expires);
        LocalDate parsedCreationDate = LocalDate.parse(creationDate.substring(0, 10));
        LocalDate today = LocalDate.now();

        LocalDate created = randomDate(parsedCreationDate, today);
        LocalDate closes = randomDate(created, parsedExpires);
        //randomly generate a time and append it to the created time string
        Time time = new Time((long)random.nextInt(24*60*60*1000));

        return new String[] {closes.toString(), created.toString() + " " + time.toString()};
    }

    /**
     * Randomly generates the quantity for sale item and remaining quantity of associated inventory item
     * @return the quantity values in a list
     */
    private int[] generateQuantities(int upperLimit) {
        // "+ 1" because the upperLimit itself is not part of the random pick
        int quantity = random.nextInt(upperLimit) + 1;
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
     * Updates the associated inventory item quantity with the new quantity deducted from the sale item generation.
     * @param remainingQuantity the remaining quantity from generating the sale item
     * @param invItemId the associated inventory item id of the generated sale item
     * @throws SQLException
     */
    private void updateInventoryItemQuantity(int remainingQuantity, long invItemId) throws SQLException {
        System.out.println(String.format("Updating inventory item quantity of id %d", invItemId));
        PreparedStatement stmt = conn.prepareStatement(
                "UPDATE inventory_item SET quantity = ? WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, remainingQuantity);
        stmt.setObject(2, invItemId);
        stmt.executeUpdate();
    }

    /**
     * Creates and inserts the sale item into the database
     * @param invItemId the id associated with the inventory item entity
     * @param invItemInfo information of the inventory item to create sales item with
     * @return the id of sale item
     */
    private long createInsertSaleItemSQL(long invItemId, String[] invItemInfo) throws SQLException {
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
        updateInventoryItemQuantity(remainingQuantity, invItemId);
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
     * @param invItemIds List of all the ids of the inventory items
     * @return
     */
    public List<Long> generateSaleItems(List<Long> invItemIds, int saleItemCount) throws SQLException {
        List<Long> generatedSaleItemIds = new ArrayList<>();
        for (int i=0; i < saleItemCount; i++) {
            clear();
            System.out.println(String.format("Creating Sale Item %d / %d", i+1, saleItemCount));
            int progress = (int) (((float)(i+1) / (float)saleItemCount) * 100);
            System.out.println(String.format("Progress: %d%%", progress));

            //Inventory items can have multiple sales, as such, if we randomize the which inventory item is chosen
            //we can allow some sale items to exist for the same inventory item.
            long invItemId = invItemIds.get(random.nextInt(invItemIds.size()));
            String[] invItemInfo = extractInvItemInfo(invItemId);
            //checks if the quantity equals to zero, if zero, it will increment the inventory item's quantity by a random number
            if (Integer.parseInt(invItemInfo[2]) == 0) {
                updateInventoryItemQuantity(random.nextInt(249) + 1, invItemId);
            }

            long saleItemId = createInsertSaleItemSQL(invItemId, invItemInfo);

            generatedSaleItemIds.add(saleItemId);
        }
        return generatedSaleItemIds;
    }
}
