package org.seng302.datagenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.clear;
import static org.seng302.datagenerator.Main.connectToDatabase;

public class ProductGenerator {
    private Random random = new Random();
    private Connection conn;
    static Scanner scanner = new Scanner(System.in);

    //predefined lists
    String[] COUNTRIES = {"New Zealand", "Australia", "Japan", "Korea", "Singapore", "Vatican City"};
    String[] DESCRIPTIONS = {"Good for your gut", "May contain traces of peanuts", "Helps improve grades"}
    String[] MANUFACTURERS = {"Nathan", "Connor", "Ella", "Josh", "Henry", "Edward", "Ben", "Kai"};
    String[] NAMES = {"Nathan Apple", "Yellow Banana", "Orange Coloured Orange", "A Box", "The Box", "Cube Shaped Box"};
    String[] PRODUCTCODES = {"APPLE123", "BANANA456", "ORANGE789"}; //Change to randomly generated?

    /**
     * Randomly generates the recommended retail price
     * @return the RRP
     */
    public float generateRRP() {
        int RRPx100 = random.nextInt(100000);
        return ((float) RRPx100) / 100;
    }


    public ProductGenerator(Connection conn) { this.conn = conn; }

    /**
     * Creates and inserts the product into the database
     * @param businessId the id associated with the business entity representing the business who owns this product
     */
    private void createInsertProductSQL(long businessId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO product (country_of_sale, created, description, manufacturer, name, product_code, " +
                        "recommended_retail_price, business_id)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        stmt.setObject(1, COUNTRIES[random.nextInt(COUNTRIES.length)]);
        stmt.setObject(2, Instant.now());
        stmt.setObject(3, DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)]);
        stmt.setObject(4, MANUFACTURERS[random.nextInt(MANUFACTURERS.length)]);
        stmt.setObject(5, NAMES[random.nextInt(NAMES.length)]);
        stmt.setObject(6, PRODUCTCODES[random.nextInt(PRODUCTCODES.length)]);
        stmt.setObject(7, generateRRP());
        stmt.setObject(8, businessId);
        stmt.executeUpdate();
    }

    private int getProductsFromInput() throws InterruptedException {
        int products = 0 //Change products
        while (products <= 0) {
            clear();
            try {
                System.out.println("-----------------------------------------");
                System.out.println("How many products do you want generated");
                System.out.println("and put into the database?");
                System.out.println("----------------------------------------");
            } catch (NoSuchElementException) {
                System.out.println("You are using the gradle generate function");
                System.out.println("This console does not support scanner inputs");
                System.out.println("To input your own number of products");
                System.out.println("Compile and run this java file in a local terminal");
                System.out.println("10 products will be creating in...");
                for (int i=5; i>0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(i);
                }
                products = 10;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return products;
    }

    /**
     * The main program
     */
    public static void main(String[] args) throws InterruptedException, SQLException {
        Connection conn = connectToDatabase();
        var generator = new ProductGenerator(conn);
        generator.generateProducts();
    }


    private void generateProducts() throws InterruptedException {
        int products = getProductsFromInput();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner.close();
    }
}
