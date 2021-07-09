package org.seng302.datagenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class ProductGeneratorTest {
    private Connection conn;
    private ProductGenerator productGenerator;

    @BeforeEach
    public void setup() throws SQLException {
        //Connects to production database
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        //change password
        this.conn = DriverManager.getConnection(url, "seng302-team500", "changeMe");

        //Creates generators
        this.productGenerator = new ProductGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        conn.close();
    }

    /**
     * Checks that the required fields within the product table are not null by querying the database
     * @param productId the id of the product
     */
    public void checkRequiredFieldsNotNull(long productId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM product WHERE id = ? AND " +
                        "country_of_sale IS NOT NULL AND created IS NOT NULL AND description IS NOT NULL AND " +
                        "manufacturer IS NOT NULL AND name IS NOT NULL AND product_code IS NOT NULL AND " +
                        "recommended_retail_price IS NOT NULL AND business_id IS NOT NULL"
        );
        stmt.setObject(1, productId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != 1) {
            fail();
        }
    }

    /**
     * Finds out the number of products within the database
     * @return the number of products within the database
     */
    public long getNumProductsInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM product");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    /**
     * Deletes the generated products form the database as part of the clean up process
     * @param productIds the ids of the generated products
     */
    public void deleteProductsFromDB(List<Long> productIds) throws SQLException {
        for (Long productId: productIds) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM product WHERE id = ?");
            stmt.setObject(1, productId);
            stmt.executeQuery();
        }
    }

    @Test
    void generateProducts_generateOneProduct_oneProductGenerated() throws SQLException {
        List<Long> productIds = productGenerator.generateProducts(1);
        if (productIds.size() != 1) {
            fail();
        }
        long productId = productIds.get(0);
        checkRequiredFieldsNotNull(productId);
        deleteProductsFromDB(productIds);
    }

    @Test
    void generateProducts_generateTwoProducts_twoProductsGenerated() throws SQLException {
        List<Long> productIds = productGenerator.generateProducts(2);
        if (productIds.size() != 2) {
            fail();
        }
        for (int i=0; i < productIds.size(); i++) {
            checkRequiredFieldsNotNull(productIds.get(i));
        }
        deleteProductsFromDB(productIds);
    }

    @Test
    void generateProducts_generateTenProducts_tenProductsGenerated() throws SQLException {
        List<Long> productIds = productGenerator.generateProducts(10);
        if (productIds.size() != 10) {
            fail();
        }
        for (int i=0; i < productIds.size(); i++) {
            checkRequiredFieldsNotNull(productIds.get(i));
        }
        deleteProductsFromDB(productIds);
    }

    @Test
    void generateProducts_generateHundredProducts_hundredProductsGenerated() throws SQLException {
        List<Long> productIds = productGenerator.generateProducts(100);
        if (productIds.size() != 100) {
            fail();
        }
        for (int i=0; i < productIds.size(); i++) {
            checkRequiredFieldsNotNull(productIds.get(i));
        }
        deleteProductsFromDB(productIds);
    }

    @Test
    void generateProducts_generateZeroProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> productIds = productGenerator.generateProducts(0);
        long productsInDBAfter = getNumProductsInDB();
        if (productsInDB != productsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateProducts_generateNegativeOneProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> productIds = productGenerator.generateProducts(-1);
        long productsInDBAfter = getNumProductsInDB();
        if (productsInDB != productsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateProducts_generateNegativeTenProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> productIds = productGenerator.generateProducts(-10);
        long productsInDBAfter = getNumProductsInDB();
        if (productsInDB != productsInDBAfter) {
            fail();
        }
    }
}
