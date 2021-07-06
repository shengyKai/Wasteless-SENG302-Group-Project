package org.seng302.datagenerator;

import java.sql.*;
import java.time.Instant;
import java.util.*;

import static org.seng302.datagenerator.Main.*;

public class ProductGenerator {
    private Random random = new Random();
    private Connection conn;

    //predefined lists
    String[] COUNTRIES = {"New Zealand", "Australia", "Japan", "Korea", "Singapore", "Vatican City"};
    String[] DESCRIPTIONS = {"Good for your gut", "May contain traces of peanuts", "Helps improve grades"};
    String[] MANUFACTURERS = {"Nathan", "Connor", "Ella", "Josh", "Henry", "Edward", "Ben", "Kai"};
    String[] NAMES = {"Nathan Apple", "Yellow Banana", "Orange Coloured Orange", "A Box", "The Box", "Cube Shaped Box"};
    String[] PRODUCTCODES = {"APPLE123", "BANANA456", "ORANGE789"}; //Change to randomly generated?

    public ProductGenerator(Connection conn) { this.conn = conn; }

    /**
     * Randomly generates the recommended retail price
     * @return the RRP
     */
    public float generateRRP() {
        int RRPx100 = random.nextInt(100000);
        return ((float) RRPx100) / 100;
    }

    /**
     * Creates and inserts the product into the database
     * @param businessId the id associated with the business entity representing the business who owns this product
     * @return the id of the generated product
     */
    private long createInsertProductSQL(long businessId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO product (country_of_sale, created, description, manufacturer, name, product_code, " +
                        "recommended_retail_price, business_id)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
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
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }

    /**
     * The main program
     */
    public static void main(String[] args) throws InterruptedException, SQLException {
        Connection conn = connectToDatabase();
        var generator = new ProductGenerator(conn);

        int productCount = getNumObjectsFromInput("products");
        generator.generateProducts(productCount);
    }

    /**
     * Generates the products
     * @param productCount Number of products to generate
     * @return List of generated product ids
     */
    public List<Long> generateProducts(int productCount) throws InterruptedException {
        var businessGenerator = new BusinessGenerator(conn);
        List<Long> generatedProductIds = new ArrayList<>();
        try {
            for (int i=0; i < productCount; i++) {
                clear();
                List<Long> businessIds = businessGenerator.generateBusinesses(1);
                long businessId = businessIds.get(0);

                System.out.println(String.format("Creating Product %d / %d", i+1, productCount));
                int progress = (int) (((float)(i+1) / (float)productCount) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long productId = createInsertProductSQL(businessId);

                generatedProductIds.add(productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return generatedProductIds;
    }
}
