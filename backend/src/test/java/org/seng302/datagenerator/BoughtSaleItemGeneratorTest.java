package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
class BoughtSaleItemGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private BoughtSaleItemGenerator boughtSaleItemGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BoughtSaleItemRepository boughtSaleItemRepository;


    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates Generators
        this.userGenerator = new UserGenerator(conn);
        this.businessGenerator = new BusinessGenerator(conn);
        this.productGenerator = new ProductGenerator(conn);
        this.boughtSaleItemGenerator = new BoughtSaleItemGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        boughtSaleItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks that the required fields within the bought sale item table are not null using an SQL query.
     * I have included all the fields including ones that are not required as the bought sale item generator
     * fills in every field.
     * @param boughtSaleItemId the id of the generated inventory item
     */
    public void checkRequiredFieldsNotNull(long boughtSaleItemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM bought_sale_item WHERE id = ? AND " +
                        "like_count IS NOT NULL AND listing_date IS NOT NULL AND price IS NOT NULL AND " +
                        "quantity IS NOT NULL AND sale_date IS NOT NULL AND buyer IS NOT NULL AND " +
                        "product_id IS NOT NULL"
        );
        stmt.setObject(1, boughtSaleItemId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        assertEquals(1, results.getLong(1));
    }

    /**
     * Queries that database to find out how many bought sale items are in the database
     * @return the number of bought sale items in the database
     */
    public long getNumBoughtSaleItemsInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM bought_sale_item");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet   ();
        results.next();
        return results.getLong(1);
    }

    /**
     * Generates a specified number of users returns the ids of the generated users.
     * @param userCount the number of users to be generated
     * @return the ids of the generated users
     */
    public List<Long> generateUsers(int userCount) {
        return userGenerator.generateUsers(userCount);
    }

    /**
     * Generates a specified number of businesses and products using the generators and returns the ids of the
     * generated products.
     * @param userIds the ids of generated users
     * @param businessCount the number of businesses to be generated
     * @param productCount the number of products to be generated
     * @return the ids of the generated products
     */
    public List<Long> generateBusinessAndProduct(List<Long> userIds, int businessCount, int productCount) {
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);
        return productGenerator.generateProducts(businessIds, productCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10, 100})
    void generateBoughtSaleItems_generateManyBoughtSaleItems_manyBoughtSaleItemsGenerated(int count) throws SQLException {
        List<Long> userIds = generateUsers(1);
        System.out.println(userIds);
        List<Long> productIds = generateBusinessAndProduct(userIds, 1, 1);
        System.out.println(productIds);
        List<Long> boughtSaleItemIds = boughtSaleItemGenerator.generateBoughtSaleItems(productIds, userIds, count);
        assertEquals(count, boughtSaleItemIds.size());
        for (long boughtSaleItemId: boughtSaleItemIds) {
            checkRequiredFieldsNotNull(boughtSaleItemId);
        }
    }

    @Test
    void generateBoughtSaleItems_generateZeroBoughtSaleItems_noBoughtSaleItemsGenerated() throws SQLException {
        long boughtSaleItemsInDB = getNumBoughtSaleItemsInDB();
        List<Long> userIds = generateUsers(1);
        List<Long> productIds = generateBusinessAndProduct(userIds, 1, 1);
        boughtSaleItemGenerator.generateBoughtSaleItems(productIds, userIds, 0);
        long boughtSaleItemsInDBAfter = getNumBoughtSaleItemsInDB();
        assertEquals(boughtSaleItemsInDB, boughtSaleItemsInDBAfter);
    }

    @Test
    void generateBoughtSaleItems_generateNegativeOneBoughtSaleItems_noBoughtSaleItemsGenerated() throws SQLException {
        long boughtSaleItemsInDB = getNumBoughtSaleItemsInDB();
        List<Long> userIds = generateUsers(1);
        List<Long> productIds = generateBusinessAndProduct(userIds, 1, 1);
        boughtSaleItemGenerator.generateBoughtSaleItems(productIds, userIds, -1);
        long boughtSaleItemsInDBAfter = getNumBoughtSaleItemsInDB();
        assertEquals(boughtSaleItemsInDB, boughtSaleItemsInDBAfter);
    }

    @Test
    void generateBoughtSaleItems_generateNegativeTenBoughtSaleItems_noBoughtSaleItemsGenerated() throws SQLException {
        long boughtSaleItemsInDB = getNumBoughtSaleItemsInDB();
        List<Long> userIds = generateUsers(1);
        List<Long> productIds = generateBusinessAndProduct(userIds, 1, 1);
        boughtSaleItemGenerator.generateBoughtSaleItems(productIds, userIds, -10);
        long boughtSaleItemsInDBAfter = getNumBoughtSaleItemsInDB();
        assertEquals(boughtSaleItemsInDB, boughtSaleItemsInDBAfter);
    }

}