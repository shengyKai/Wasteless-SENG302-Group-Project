package org.seng302.datagenerator;

import io.cucumber.java.sl.In;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
class BusinessGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private InventoryItemGenerator inventoryItemGenerator;
    private SaleItemGenerator saleItemGenerator;
    private BoughtSaleItemGenerator boughtSaleItemGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;
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
        this.inventoryItemGenerator = new InventoryItemGenerator(conn);
        this.saleItemGenerator = new SaleItemGenerator(conn);
        this.boughtSaleItemGenerator = new BoughtSaleItemGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        boughtSaleItemRepository.deleteAll();
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks that the required fields within the business table are not null using an SQL query
     * @param businessId the id of the generated business
     */
    public void checkRequiredFieldsNotNull(long businessId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM business WHERE id = ? AND " +
                        "business_type IS NOT NULL AND created IS NOT NULL AND description IS NOT NULL AND " +
                        "name IS NOT NULL AND address_id IS NOT NULL AND owner_id IS NOT NULL"
        );
        stmt.setObject(1, businessId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        assertEquals(1, results.getLong(1));
    }

    /**
     * Queries that database to find out how many businesses are in the database
     * @return the number of businesses in the database
     */
    public long getNumBusinessesInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM business");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    @ParameterizedTest
    @ValueSource(ints={1, 2, 10, 100})
    void generateBusinesses_generateSomeBusinessesAndConsistentData_correctNumberOfBusinessesGenerated(int count) throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(2);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, count);
        assertEquals(count, businessIds.size());
        for (Long businessId : businessIds) {
            checkRequiredFieldsNotNull(businessId);
        }
    }

    @Test
    void generateBusinesses_generateZeroBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        businessGenerator.generateBusinesses(userIds, 0);
        long businessesInDBAfter = getNumBusinessesInDB();
        assertEquals(businessesInDB, businessesInDBAfter);
    }

    @Test
    void generateBusinesses_generateNegativeOneBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        businessGenerator.generateBusinesses(userIds, -1);
        long businessesInDBAfter = getNumBusinessesInDB();
        assertEquals(businessesInDB, businessesInDBAfter);
    }

    @Test
    void generateBusinesses_generateNegativeTenBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        businessGenerator.generateBusinesses(userIds, -10);
        long businessesInDBAfter = getNumBusinessesInDB();
        assertEquals(businessesInDB, businessesInDBAfter);
    }

    @ParameterizedTest
    @CsvSource({"13,0,13", "0,101,101", "0,0,0", "44,18,62"})
    void setBusinessPointsFromSaleItems_pointsSetToSumOfBoughtAndCurrentSaleItems(int saleItemCount, int boughtSaleItemCount, long expectedPoints) throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 1);
        List<Long> productIds = productGenerator.generateProducts(businessIds, 1);
        List<Long> invItemIds = inventoryItemGenerator.generateInventoryItems(productIds, 1);
        saleItemGenerator.generateSaleItems(invItemIds, saleItemCount);
        boughtSaleItemGenerator.generateBoughtSaleItems(productIds, userIds, boughtSaleItemCount);
        businessGenerator.setBusinessPointsFromSaleItems(businessIds);
        Business business = businessRepository.findById(businessIds.get(0)).orElseThrow();
        assertEquals(expectedPoints, business.getPoints());
    }

}
