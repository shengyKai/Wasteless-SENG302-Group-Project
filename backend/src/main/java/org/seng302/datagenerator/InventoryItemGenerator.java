package org.seng302.datagenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.clear;
import static org.seng302.datagenerator.Main.connectToDatabase;

public class InventoryItemGenerator {
    private Random random = new Random();
    private Connection conn;
    static Scanner scanner = new Scanner(System.in);

    //predefined lists

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
     */
    private void createInsertInventoryItemSQL(long productId) throws SQLException {
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
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
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
    }

    /**
     * Asks the user how many inventory items they want generated
     * @return the number of inventory items generated
     */
    private int getInventoryItemsFromInput() throws InterruptedException {
        int inventoryItems = 0; //Change inventory items
        while (inventoryItems <= 0) {
            clear();
            try {
                System.out.println("------------------------------------");
                System.out.println("How many inventory items do you want");
                System.out.println("generated and put into the database?");
                System.out.println("------------------------------------");
                inventoryItems = Integer.parseInt(scanner.nextLine());
            } catch (NoSuchElementException e) {
                System.out.println("You are using the gradle generate function");
                System.out.println("This console does not support scanner inputs");
                System.out.println("To input your own number of inventory items");
                System.out.println("Compile and run this java file in a local terminal");
                System.out.println("10 inventory items will be creating in...");
                for (int i=5; i>0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(i);
                }
                inventoryItems = 10;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return inventoryItems;
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new InventoryItemGenerator(conn);
        generator.generateInventoryItems();
    }

    private void generateInventoryItems() throws InterruptedException {
        int inventoryItems = getInventoryItemsFromInput();

    }
}
