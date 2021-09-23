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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
class SaleItemGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private InventoryItemGenerator invItemGenerator;
    private SaleItemGenerator saleItemGenerator;

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
        this.saleItemGenerator = new SaleItemGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks that the required fields within the sale item table are not null using an SQL query.
     * @param saleItemId the id of the generated sale item
     */
    public void checkRequiredFieldsNotNull(long saleItemId) throws SQLException {
      PreparedStatement stmt = conn.prepareStatement(
              "SELECT COUNT(*) FROM sale_item WHERE id = ? AND " +
                      "price IS NOT NULL AND quantity IS NOT NULL AND inventory_item_id IS NOT NULL"
      );
      stmt.setObject(1, saleItemId);
      stmt.executeQuery();
      ResultSet results = stmt.getResultSet();
      results.next();
      assertEquals(1, results.getLong(1));
    } 

    /**
     * Queries that database to find out how many sale items are in the database
     * @return the number of sale items in the database
     */
    public long getNumSaleItemsInDB() throws SQLException {
      PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM sale_item");
      stmt.executeQuery();
      ResultSet results = stmt.getResultSet();
      results.next();
      return results.getLong(1);
    }

    /**
     * Generates a specified number of users, businesses, products and inventory items using the generators and returns the ids of the
     * generated inventory items.
     * @param userCount the number of users to be generated
     * @param businessCount the number of businesses to be generated
     * @param productCount the number of products to be generated
     * @param invItemCount the number of inventory items to be generated
     * @return the ids of the generated inventory items
     */
    public List<Long> generateUserBusinessProductAndInvItems(int userCount, int businessCount, int productCount, int invItemCount) throws SQLException {
      List<Long> userIds = userGenerator.generateUsers(userCount);
      List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);
      List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);
      return invItemGenerator.generateInventoryItems(productIds, invItemCount);
  }


    @ParameterizedTest
    @ValueSource(ints={1, 2, 10, 100})
    void generateSaleItems_generateSomeSaleItemsAndConsistentData_correctNumberOfSaleItemsGenerated(int count) throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, count);
        assertEquals(count, saleItemIds.size());
        for (long saleItemId: saleItemIds) {
            checkRequiredFieldsNotNull(saleItemId);
        }
    }

    @Test
    void generateSaleItems_generateZeroSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        saleItemGenerator.generateSaleItems(invItemIds, 0);
        long saleItemsInDB = getNumSaleItemsInDB();
        assertEquals(0, saleItemsInDB);
    }

    @Test
    void generateSaleItems_generateNegativeOneSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        saleItemGenerator.generateSaleItems(invItemIds, -1);
        long saleItemsInDB = getNumSaleItemsInDB();
        assertEquals(0, saleItemsInDB);
    }

    @Test
    void generateSaleItems_generateNegativeTenSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        saleItemGenerator.generateSaleItems(invItemIds, -10);
        long saleItemsInDB = getNumSaleItemsInDB();
        assertEquals(0, saleItemsInDB);
    }

    @Test
    void generateSaleItems_generateSaleItemFromInvItemWithZeroQuantity_oneSaleItemGenerated() throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        Method updateInventoryItemQuantity = SaleItemGenerator.class.getDeclaredMethod("updateInventoryItemQuantity", int.class, long.class);
        updateInventoryItemQuantity.setAccessible(true);
        updateInventoryItemQuantity.invoke(saleItemGenerator, 0, invItemIds.get(0));
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 1);
        assertEquals(1, saleItemIds.size());
        long saleItemId = saleItemIds.get(0);
        checkRequiredFieldsNotNull(saleItemId);
    }

}
