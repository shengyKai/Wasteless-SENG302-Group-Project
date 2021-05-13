package org.seng302.entities;

import org.junit.jupiter.api.*;
import org.seng302.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaleItemTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryItemRepository inventoryItemRepository;
    @Autowired
    SaleItemRepository saleItemRepository;

    InventoryItem invItem;

    void createTestObjects() throws Exception {
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

        Business testBusiness = new Business.Builder()
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
        Product testProduct = productRepository.save(product);

        invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy("2021-05-21")
                .withBestBefore("2021-05-28")
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
    }

    /**
     * Deletes all entries from the database
     */
    void clearDatabase() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeAll
    void setUp() throws Exception {
        createTestObjects();
    }

    @AfterAll
    void tearDown() {
        clearDatabase();
    }

    @Test
    void createSaleItem_AllFieldsCorrect_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_NoInventoryItem_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("200.34")
                    .withQuantity(2)
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Cannot sell something that is not in your inventory", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void deleteInventoryItem_SaleItemDeleted() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);

        inventoryItemRepository.deleteAll();
        Optional<SaleItem> foundItem = saleItemRepository.findById(saleItem.getSaleId());
        if (foundItem.isPresent()) { Assertions.fail(); }
    }
}
