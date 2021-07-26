package org.seng302.datagenerator;

import java.net.URISyntaxException;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import static org.seng302.datagenerator.Main.*;

public class ProductGenerator {
    private final Random random = new Random();
    private final Connection conn;
    private final CommerceNameGenerator commerceNameGenerator = CommerceNameGenerator.getInstance();
    private final ProductImageGenerator imageGenerator;


    //predefined lists
    String[] DESCRIPTIONS = {"Good for your gut", "May contain traces of peanuts", "Helps improve grades"};
    //TODO when description generator finished hook it up

    public ProductGenerator(Connection conn) {
        this.conn = conn;
        this.imageGenerator = new ProductImageGenerator(conn);
    }

    /**
     * Randomly generates the recommended retail price
     * @return the RRP
     */
    public float generateRRP() {
        int RRPx100 = random.nextInt(100000);
        return ((float) RRPx100) / 100;
    }

    /**
     * Randomly generates a product code
     * @return the randomly generated product code
     */
    public String generateProductCode() {
        String productWord = "";
        int numLetters = random.nextInt(20);
        for (int i=0; i < numLetters; i++) {
            productWord += random.nextInt(26) + 'A';
        }
        int productNumber = random.nextInt(99999);
        return productWord + productNumber;
    }

    /**
     * Retrieves the country the business is located in by querying the database
     * @param businessId the id of the business
     * @return the country of the business
     */
    private String getCountryOfBusiness(long businessId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT country FROM location WHERE id = " +
                        "(SELECT address_id FROM business WHERE id = ?)"
        );
        stmt.setObject(1, businessId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getString(1);
    }

    /**
     * Creates and inserts the product into the database
     * @param businessId the id associated with the business entity representing the business who owns this product
     * @return the id of the generated product
     */
    private long createInsertProductSQL(long businessId, boolean generateImages) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO product (country_of_sale, created, description, manufacturer, name, product_code, " +
                        "recommended_retail_price, business_id)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        String productName = commerceNameGenerator.randomProductName();

        stmt.setObject(1, getCountryOfBusiness(businessId));
        stmt.setObject(2, Instant.now());
        stmt.setObject(3, DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)]);
        stmt.setObject(4, commerceNameGenerator.randomManufacturerName());
        stmt.setObject(5, productName);
        stmt.setObject(6, generateProductCode());
        stmt.setObject(7, generateRRP());
        stmt.setObject(8, businessId);
        System.out.println(stmt);
        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        long productId = keys.getLong(1);
        if (generateImages) imageGenerator.addImageToProduct(productId, productName);
        return productId;
    }

    /**
     * The main program
     */
    public static void main(String[] args) throws InterruptedException, SQLException {
        Connection conn = connectToDatabase();
        var userGenerator = new UserGenerator(conn);
        var businessGenerator = new BusinessGenerator(conn);
        var productGenerator = new ProductGenerator(conn);

        int userCount = getNumObjectsFromInput("users");
        List<Long> userIds = userGenerator.generateUsers(userCount);

        int businessCount = getNumObjectsFromInput("businesses");
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);

        int productCount = getNumObjectsFromInput("products");
        List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);
    }

    /**
     * Generates the products
     * @param productCount Number of products to generate
     * @param businessIds List of business IDs
     * @param generateImages Whether to generate images for the products
     * @return List of generated product ids
     */
    public List<Long> generateProducts(List<Long> businessIds, int productCount, boolean generateImages) {
        List<Long> generatedProductIds = new ArrayList<>();
        try {
            for (int i=0; i < productCount; i++) {
                clear();
                long businessId = businessIds.get(0);

                System.out.println(String.format("Creating Product %d / %d", i+1, productCount));
                int progress = (int) (((float)(i+1) / (float)productCount) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long productId = createInsertProductSQL(businessId, generateImages);

                generatedProductIds.add(productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return generatedProductIds;
    }
    /**
     * Generates the products with images disabled
     * @param productCount Number of products to generate
     * @param businessIds List of business IDs
     * @return List of generated product ids
     */
    public List<Long> generateProducts(List<Long> businessIds, int productCount) {
        return generateProducts(businessIds, productCount, false);
    }

}
