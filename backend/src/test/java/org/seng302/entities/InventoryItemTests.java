package org.seng302.entities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private InventoryItem testInvItem;

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

    @BeforeEach
    void setUp() throws ParseException {
        createTestObjects();
    }

    @AfterAll
    void tearDown() {
        clearDatabase();
    }

    /**
     * Creates an inventory with all the fields.
     */
    @Test
    void createInventoryItem_withAllFields_ObjectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem(2.69)
                .withManufactured("2021-03-11")
                .withSellBy("2021-05-21")
                .withBestBefore("2021-05-28")
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
        testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        //Add custom equals to inventory item
        assertEquals(invItem.getId(), testInvItem.getId());
    }

    /**
     * Creates an inventory that only contains the required fields.
     */
    @Test
    void createInventoryItem_withOnlyRequiredFields_ObjectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
        testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        //Add custom equals to inventory item
        assertEquals(invItem.getId(), testInvItem.getId());
    }

    //write tests for each individual null
    //write tests for testing many to one relationship of product
    //write tests for testing retrieving product
    //write tests for builder
}
