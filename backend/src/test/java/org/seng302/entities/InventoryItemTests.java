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

    @Test
    void createInventoryItem_withNullProduct_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(null)
                    .withQuantity(2)
                    .withExpires("2021-06-01")
                    .build();
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No product was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

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

    @Test
    void createInventoryItem_withNullTotalPriceAndNoPricePerItem_objectCreated() throws Exception {
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

    @Test
    void createInventoryItem_withNullTotalPriceAndHasPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withPricePerItem("6.90")
                .withTotalPrice(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }

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
    @Test
    void createInventoryItem_withDateBeforeBestBefore_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    //at least 1 day later
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .withBestBefore(acceptDate.toString())
                .build();
        
        assertNotNull(invItem);
    }
    @Test
    void createInventoryItem_withWrongBestBeforeFormat_objectNotCreated() throws Exception {

        assertThrows(ParseException.class, () -> {
            InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires("2021-06-01")
            .withBestBefore("2020-061-01")
            .build();
        });
    }
}
