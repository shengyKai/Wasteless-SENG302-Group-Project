package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaleItemRepositoryTest {

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
    private InterestEventRepository interestEventRepository;

    private SaleItem testSaleItem;
    private User testUser;

    @BeforeEach
    void setUp() {
        var userLocation = new Location.Builder()
                .atDistrict("District")
                .atStreetNumber("45")
                .onStreet("Street place")
                .inCountry("Australia")
                .inRegion("NSW")
                .atDistrict("someplace")
                .inCity("Canberra")
                .withPostCode("5011").build();
        var testBusinessLocation = new Location.Builder()
                .atDistrict("District")
                .atStreetNumber("45")
                .onStreet("Street place")
                .inCountry("Australia")
                .inRegion("NSW")
                .atDistrict("someplace")
                .inCity("Canberra")
                .withPostCode("5011").build();
        testUser = new User.Builder()
                .withAddress(userLocation)
                .withDob("2000-01-01")
                .withEmail("pog32@gmail.com")
                .withFirstName("Greg")
                .withLastName("Jones")
                .withPassword("password123").build();
        testUser = userRepository.save(testUser);
        var testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(testBusinessLocation)
                .withName("Gregs pies")
                .withDescription("We enjoy pies")
                .withPrimaryOwner(testUser).build();
        testBusiness = businessRepository.save(testBusiness);
        var testProduct = new Product.Builder()
                .withName("Simple Pie")
                .withProductCode("GFD432")
                .withDescription("Yummy")
                .withBusiness(testBusiness)
                .withManufacturer("Good pies")
                .withRecommendedRetailPrice("54").build();
        testProduct = productRepository.save(testProduct);
        var testInventoryItem = new InventoryItem.Builder()
                .withPricePerItem("12")
                .withQuantity(3)
                .withTotalPrice("24")
                .withProduct(testProduct)
                .withExpires(LocalDate.now().plusYears(1).toString()).build();
        testInventoryItem = inventoryItemRepository.save(testInventoryItem);
        testSaleItem = new SaleItem.Builder()
                .withInventoryItem(testInventoryItem)
                .withCloses(LocalDate.now().plusYears(1).toString())
                .withQuantity(1)
                .withPrice("5")
                .withMoreInfo("yummy plz buy my pies").build();
        testSaleItem = saleItemRepository.save(testSaleItem);
    }

    @AfterEach
    void tearDown() {
        interestEventRepository.deleteAll();
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void delete_noInterestEventsForSaleItem_saleItemDeleted() {
        assertDoesNotThrow(() -> saleItemRepository.delete(testSaleItem));
        assertTrue(saleItemRepository.findById(testSaleItem.getId()).isEmpty());
    }

    @Test
    void delete_interestEventExistsForSaleItem_saleItemAndInterestEventDeleted() {
        var interestEvent = interestEventRepository.save(new InterestEvent(testUser, testSaleItem));

        assertDoesNotThrow(() -> saleItemRepository.delete(testSaleItem));
        assertTrue(saleItemRepository.findById(testSaleItem.getId()).isEmpty());
        assertTrue(interestEventRepository.findById(interestEvent.getId()).isEmpty());
    }

}