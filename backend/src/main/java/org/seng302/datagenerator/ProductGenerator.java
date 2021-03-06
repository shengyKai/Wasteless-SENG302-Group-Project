package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ProductGenerator {
    private final Random random = new Random();
    private final Connection conn;
    private final CommerceNameGenerator commerceNameGenerator = CommerceNameGenerator.getInstance();
    private final DescriptionGenerator descriptionGenerator = DescriptionGenerator.getInstance();
    private final ProductImageGenerator imageGenerator;
    private final HashSet<String> productCodeHash = new HashSet<>();
    private final Logger logger = LogManager.getLogger(ProductGenerator.class.getName());

    public ProductGenerator(Connection conn) {
        this.conn = conn;
        this.imageGenerator = new ProductImageGenerator(conn);
    }

    /**
     * Randomly generates the recommended retail price
     * @return the RRP
     */
    public float generateRRP() {
        int rrpTimes100 = random.nextInt(100000);
        return ((float) rrpTimes100) / 100;
    }

    /**
     * Randomly generates a product code
     * @return the randomly generated product code
     */
    public String generateProductCode() {
        StringBuilder productWord = new StringBuilder();
        for (int i=0; i < 9; i++) {
            char character = (char) (random.nextInt(26) + 'A');
            productWord.append(character);
        }

        int productNumber;
        String productCode;

        do {
            productNumber = random.nextInt(999999);
            productCode = productWord.toString() + productNumber;
        } while (productCodeHash.contains(productCode));

        productCodeHash.add(productCode);
        return productCode;
    }

    /**
     * Retrieves the country the business is located in by querying the database
     * @param businessId the id of the business
     * @return the country of the business
     */
    private String getCountryOfBusiness(long businessId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT country FROM location WHERE id = " +
                        "(SELECT address_id FROM business WHERE id = ?)"
        )) {
            stmt.setObject(1, businessId);
            stmt.executeQuery();
            ResultSet results = stmt.getResultSet();
            results.next();
            return results.getString(1);
        }
    }

    /**
     * Creates and inserts the product into the database
     * @param businessId the id associated with the business entity representing the business who owns this product
     * @return the id of the generated product
     */
    private long createInsertProductSQL(long businessId, boolean generateImages) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO product (country_of_sale, created, description, manufacturer, name, product_code, " +
                        "recommended_retail_price, business_id)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            String productName = commerceNameGenerator.randomProductName();

            stmt.setObject(1, getCountryOfBusiness(businessId));
            stmt.setObject(2, Instant.now());
            stmt.setObject(3, descriptionGenerator.randomDescription());
            stmt.setObject(4, commerceNameGenerator.randomManufacturerName());
            stmt.setObject(5, productName);
            stmt.setObject(6, generateProductCode());
            stmt.setObject(7, generateRRP());
            stmt.setObject(8, businessId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            long productId = keys.getLong(1);
            if (generateImages) imageGenerator.addImageToProduct(productId, productName);
            return productId;
        }
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
                long businessId = businessIds.get(random.nextInt(businessIds.size()));

                logger.info("Creating Product {} / {}", i+1, productCount);
                int progress = (int) (((float)(i+1) / (float)productCount) * 100);
                logger.info("Progress: {}%", progress);
                long productId = createInsertProductSQL(businessId, generateImages);

                generatedProductIds.add(productId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
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
