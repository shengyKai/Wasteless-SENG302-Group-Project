package org.seng302.entities;

import org.junit.jupiter.api.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryItemTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryItemRepository inventoryItemRepository;

    private Business testBusiness;
    private Product testProduct;

    void createTestObjects() throws ParseException {
        clearDatabase();
        User testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);

        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName1")
                .withPrimaryOwner(testUser)
                .build();
        testBusiness = businessRepository.save(testBusiness);

        Product product = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness)
                .build();
        testProduct = productRepository.save(product);
    }

    /**
     * Deletes all entries from the database
     */
    void clearDatabase() {
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }
    @BeforeAll
    void setUp() throws ParseException {
        createTestObjects();
    }
    @AfterAll
    void tearDown() {
        clearDatabase();
    }
    /**
     * Check when all attributes is provided and in correct format, object should be created
     * @throws Exception
     */
    @Test
    void createInventoryItem_withAllFields_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy("2021-05-21")
                .withBestBefore("2021-05-28")
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Check when only mandatory attributes is provided and in correct format, object should be created
     * @throws Exception
     */
    @Test
    void createInventoryItem_withOnlyRequiredFields_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }

    /**
     * Test that check mandatory cant be null
    /**

     * Create object with Null product, should not pass as product is mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullProduct_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(null)
                    .withQuantity(2)
                    .withExpires("2021-06-01")
                    .build();
            // fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No product was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Create object with Null expires, should not pass as expires is mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullExpires_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(1)
                    .withExpires(null)
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No expiry date was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    /**
     * Create object with quantity = 0, should not pass as quantity is mandatory and need to be greater than 0
     * @throws Exception
     */
    @Test
    void createInventoryItem_withZeroQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(0)
                    .withExpires("2021-06-01")
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    /**
     * Create object with quantity = 0, should not pass as quantity is mandatory and need to be greater than 0
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNegativeQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(-69)
                    .withExpires("2021-06-01")
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * The following test section will test attributes related to price which is 
     * Price per item, Total price
     * Test condition will be (Null, validPriceFormat, InvalidPriceFormat, 
     * LowerThanPriceRange, HigherThanPriceRange, ExactlyOnPriceRangeEnd)
    /** 

    /**
     * Create object with Pricer Per Item = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withPricePerItem(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create object with Total Price     = null, should pass as the attribute is not mandatory
     * Create object with Pricer Per Item = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullTotalPriceAndNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withPricePerItem(null)
                .withTotalPrice(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create object with Total Price = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withTotalPrice(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
        /**
     * Create object with Valid Price per item , should pass
     * @throws Exception
     */
    @Test
    void createInventoryItem_withPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withPricePerItem("2.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create object with Valid Total Price, should pass
     * @throws Exception
     */
    @Test
    void createInventoryItem_withTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withTotalPrice("21.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create object with a Invalid Price Per Item format, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withInvalidPricePerItemFormat_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("xx0.1")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a Invalid Total Price format, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withInvalidTotalPriceFormat_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withTotalPrice("xx0.1")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a Price lower than PricePerItem range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNegativePricePerItem_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("-2")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a Price lower than totalPrice range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNegativeTotalPrice_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("-2")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a Price higher than PricePerItem range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withHigherPricePerItem_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("10001")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a Price higher than totalPrice range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withHigherTotalPrice_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("1000001")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a allowed highest value in PricePerItem range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withHighestPricePerItem_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("10000")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create object with a allowed highest value in totalPrice range, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withHigestTotalPrice_objectNotCreated() throws Exception {
        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withPricePerItem("1000000")
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * The following test section will test attributes related to date which is 
     * Manufactured, Sell By, best Before
     * Test condition will be ( Null, dayBeforeToday, dayAfterToday, InvalidDateFormat)
    /**
     * 
     * Create object with Manufactured = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullManufacturedDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withManufactured(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create Manufactured with a date from the past (1 day before), should pass
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInPastForManufactured_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withManufactured(acceptDate.toString())
        .withExpires("2021-06-01")
        .build();
        assertNotNull(invItem);

    }
    /**
     * Create Manufactured with a date from the future ( 1 day later), should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInFutureForManufactured_objectNotCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);   
        assertThrows(ResponseStatusException.class, () -> {                 
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(2)
                    .withManufactured(acceptDate.toString())
                    .withExpires("2021-06-01")
                    .build();
        });
    }
    /**
     * Create Manufactured with a Invalid date format, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withInvalidManufacturedFormat_objectNotCreated() throws Exception {

        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires("2021-06-01")
            .withBestBefore("2020-01-0x")
            .build();
        });
    }
    /**
     * Create object with SellBy = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullSellByDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withSellBy(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create Sell By with a date from the past (1 day before), should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInPastForSellBy_objectNotCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);                    
        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withSellBy(acceptDate.toString())
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create Sell By with a date from the future ( 1 day later), should pass
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInFutureForSellBy_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withSellBy(acceptDate.toString())
                .withExpires("2021-06-01")
                .build();
        
        assertNotNull(invItem);
    }
    /**
     * Create Sell By with a Invalid date format, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withInvalidSellByFormat_objectNotCreated() throws Exception {

        assertThrows(ParseException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires("2021-06-01")
            .withSellBy("20x-01-01")
            .build();
        });
    }
    /**
     * Create object with BestBefore = null, should pass as the attribute is not mandatory
     * @throws Exception
     */
    @Test
    void createInventoryItem_withNullBestBefore_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withBestBefore(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Create Best before with a date from the past (1 day before), should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInPastForBestBefore_objectNotCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);                    
        assertThrows(ResponseStatusException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withBestBefore(acceptDate.toString())
            .withExpires("2021-06-01")
            .build();
        });
    }
    /**
     * Create Best before with a date from the future ( 1 day later), should pass
     * @throws Exception
     */
    @Test
    void createInventoryItem_withDateInFutureForBestBefore_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withBestBefore(acceptDate.toString())
                .withExpires("2021-06-01")
                .build();
        
        assertNotNull(invItem);
    }
    /**
     * Create Best before with a Invalid date format, should fail
     * @throws Exception
     */
    @Test
    void createInventoryItem_withInvalidBestBeforeFormat_objectNotCreated() throws Exception {

        assertThrows(ParseException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires("2021-06-01")
            .withBestBefore("2020-x-01")
            .build();
        });
    }





    /**
     * Create Inventory item with same product
     * @throws Exception
     */
    @Test
    void createInventoryItem_multipleInventoryItemsHaveSameProduct_objectCreated() throws Exception {
        InventoryItem invItem1 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .build();
        InventoryItem invItem2 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires("2023-07-22")
                .build();
        InventoryItem invItem3 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(4)
                .withExpires("2025-11-06")
                .build();
        inventoryItemRepository.save(invItem1);
        inventoryItemRepository.save(invItem2);
        inventoryItemRepository.save(invItem3);
        InventoryItem testInvItem1 = inventoryItemRepository.findById(invItem1.getId()).get();
        InventoryItem testInvItem2 = inventoryItemRepository.findById(invItem2.getId()).get();
        InventoryItem testInvItem3 = inventoryItemRepository.findById(invItem3.getId()).get();
        assertEquals(invItem1, testInvItem1);
        assertEquals(invItem2, testInvItem2);
        assertEquals(invItem3, testInvItem3);
    }


    
}
