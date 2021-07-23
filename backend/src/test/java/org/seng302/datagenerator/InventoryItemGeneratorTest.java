package org.seng302.datagenerator;
import org.seng302.leftovers.Main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class InventoryItemGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private InventoryItemGenerator invItemGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

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
        this.invItemGenerator = new InventoryItemGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks that the required fields within the inventory item table are not null using an SQL query.
     * I have included all the fields including ones that are not required as the inventory item generator
     * fills in every field.
     * @param invItemId the id of the generated inventory item
     */
    public void checkRequiredFieldsNotNull(long invItemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM inventory_item WHERE id = ? AND " +
                        "best_before IS NOT NULL AND creation_date IS NOT NULL AND expires IS NOT NULL AND " +
                        "manufactured IS NOT NULL AND price_per_item IS NOT NULL AND quantity IS NOT NULL AND " +
                        "remaining_quantity IS NOT NULL AND sell_by IS NOT NULL AND total_price IS NOT NULL AND " +
                        "version IS NOT NULL AND product_id IS NOT NULL"
        );
        stmt.setObject(1, invItemId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != 1) {
            fail();
        }
    }

    /**
     * Queries that database to find out how many inventory items are in the database
     * @return the number of inventory items in the database
     */
    public long getNumInvItemsInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM inventory_item");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet   ();
        results.next();
        return results.getLong(1);
    }

    /**
     * Generates a specified number of users, businesses and products using the generators and returns the ids of the
     * generated products.
     * @param userCount the number of users to be generated
     * @param businessCount the number of businesses to be generated
     * @param productCount the number of products to be generated
     * @return the ids of the generated products
     */
    public List<Long> generateUserBusinessAndProduct(int userCount, int businessCount, int productCount) {
        List<Long> userIds = userGenerator.generateUsers(userCount);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);
        List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);
        return productIds;
    }

    @Test
    void generateInvItems_generateOneInvItemAndConsistentData_oneInvItemGenerated() throws SQLException {
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, 1);
        if (invItemIds.size() != 1) {
            fail();
        }
        long invItemId = invItemIds.get(0);
        checkRequiredFieldsNotNull(invItemId);
    }

    @Test
    void generateInvItems_generateTwoInvItemsAndConsistentData_twoInvItemsGenerated() throws SQLException {
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, 2);
        if (invItemIds.size() != 2) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
    }

    @Test
    void generateInvItems_generateTenInvItemsAndConsistentData_tenInvItemsGenerated() throws SQLException {
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, 10);
        if (invItemIds.size() != 10) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
    }

    @Test
    void generateInvItems_generateHundredInvItemsAndConsistentData_hundredInvItemsGenerated() throws SQLException {
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, 100);
        if (invItemIds.size() != 100) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
    }

    @Test
    void generateInvItems_generateZeroInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, 0);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateInvItems_generateNegativeOneInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, -1);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateInvItems_generateNegativeTenInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> productIds = generateUserBusinessAndProduct(1, 1, 1);
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, -10);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }
    //TODO Add verification of date data
}
