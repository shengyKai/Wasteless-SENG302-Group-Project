package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class SaleItemGeneratorTest {
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
              "SELECT COUNT(*) FROM sale_item WHERE sale_id = ? AND " +
                      "price IS NOT NULL AND quantity IS NOT NULL AND inventory_item_id IS NOT NULL"
      );
      stmt.setObject(1, saleItemId);
      stmt.executeQuery();
      ResultSet results = stmt.getResultSet();
      results.next();
      if (results.getLong(1) != 1) {
          fail();
      }
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
     * @throws SQLException
     */
    public List<Long> generateUserBusinessProductAndInvItems(int userCount, int businessCount, int productCount, int invItemCount) throws SQLException {
      List<Long> userIds = userGenerator.generateUsers(userCount);
      List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);
      List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);
      List<Long> invItemIds = invItemGenerator.generateInventoryItems(productIds, invItemCount);
      return invItemIds;
  }

    @Test
    void generateSaleItems_generateOneSaleItemAndConsistentData_oneSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 1);
        assertEquals(1, saleItemIds.size());
        long saleItemId = saleItemIds.get(0);
        checkRequiredFieldsNotNull(saleItemId);
    }

    @Test
    void generateSaleItems_generateTwoSaleItemsAndConsistentData_twoSaleItemsGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 2);
        assertEquals(2, saleItemIds.size());
        for (long saleItemId: saleItemIds) {
            checkRequiredFieldsNotNull(saleItemId);
        }
    }

    @Test
    void generateSaleItems_generateTenSaleItemsAndConsistentData_tenSaleItemsGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 10);
        assertEquals(10, saleItemIds.size());
        for (long saleItemId: saleItemIds) {
            checkRequiredFieldsNotNull(saleItemId);
        }
    }

    @Test
    void generateSaleItems_generateHundredSaleItemsAndConsistentData_hundredSaleItemsGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 100);
        assertEquals(100, saleItemIds.size());
        for (long saleItemId: saleItemIds) {
            checkRequiredFieldsNotNull(saleItemId);
        }
    }

    @Test
    void generateSaleItems_generateZeroSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, 0);
        long saleItemsInDB = getNumSaleItemsInDB();
        assertEquals(0, saleItemsInDB);
    }

    @Test
    void generateSaleItems_generateNegativeOneSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, -1);
        long saleItemsInDB = getNumSaleItemsInDB();
        assertEquals(0, saleItemsInDB);
    }

    @Test
    void generateSaleItems_generateNegativeTenSaleItemsAndConsistentData_NoSaleItemGenerated() throws SQLException {
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        List<Long> saleItemIds = saleItemGenerator.generateSaleItems(invItemIds, -10);
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

    @Test
    void generateQuantities_generateQuantityWithAUpperBoundOne_quantityGeneratedEqualsOne() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method generateQuantities = SaleItemGenerator.class.getDeclaredMethod("generateQuantities", int.class);
        generateQuantities.setAccessible(true);
        int[] quantities = (int[]) generateQuantities.invoke(saleItemGenerator, 1);
        assertEquals(1, quantities[0]);
    }

    @Test
    void generateQuantities_generateQuantityWithAUpperBoundFive_quantityGeneratedBetweenOneAndFive() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method generateQuantities = SaleItemGenerator.class.getDeclaredMethod("generateQuantities", int.class);
        generateQuantities.setAccessible(true);
        int[] quantities = (int[]) generateQuantities.invoke(saleItemGenerator, 5);
        assertTrue(quantities[0] >= 1);
        assertTrue(quantities[0] <= 5);
    }

    @Test
    void generateQuantities_generateQuantityWithAUpperBoundNegativeAmount_illegalArgumentExceptionThrown() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method generateQuantities = SaleItemGenerator.class.getDeclaredMethod("generateQuantities", int.class);
        generateQuantities.setAccessible(true);

        var exception = assertThrows(InvocationTargetException.class, () -> {
            generateQuantities.invoke(saleItemGenerator, -1);
        });

        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
    }

    @Test
    void generateDates_generatedDatesIsWithinExpiresAndCreationDateContraint_closesAndCreatedDateGenerated() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method generateDates = SaleItemGenerator.class.getDeclaredMethod("generateDates", String.class, String.class);
        generateDates.setAccessible(true);
        // the first parameter of generateDates() refers to the expires field of the inventory item, and the date would have to be after today. 
        // Same logic for the second parameter, creationDate, except it has to be before today. 20 is just an arbitrary number. 
        String[] generatedDates = (String[]) generateDates.invoke(saleItemGenerator, LocalDate.now().plusDays(20).toString(), LocalDate.now().minusDays(20).toString());

        LocalDate closes = LocalDate.parse(generatedDates[0]);
        LocalDate created = LocalDate.parse(generatedDates[1].substring(0, 10));

        assertTrue(closes.compareTo(created) >= 0);
    }

    @Test
    void extractInvItemInfo_informationForInventoryItemRetrievedWithInventoryItemId_inventoryItemInfoRetrievedFromDb() throws NoSuchMethodException, SecurityException, SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method extractInvItemInfo = SaleItemGenerator.class.getDeclaredMethod("extractInvItemInfo", long.class);
        extractInvItemInfo.setAccessible(true);
        List<Long> invItemIds = generateUserBusinessProductAndInvItems(1, 1, 1, 1);
        String[] invItemInfo = (String[]) extractInvItemInfo.invoke(saleItemGenerator, invItemIds.get(0));
        assertEquals(3, invItemInfo.length);
    }
}
