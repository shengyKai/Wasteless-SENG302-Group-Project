package org.seng302.leftovers.persistence.event;

import org.junit.jupiter.api.*;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterestEventRepositoryTest {

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

    @Autowired
    private EventRepository eventRepository;

    private User testUser;
    private InventoryItem inventoryItem;
    private SaleItem saleItem;

    private void deleteAll() {
        eventRepository.deleteAll();
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeAll
    void init() {
        deleteAll();
    }

    @BeforeEach
    void setup() {
        testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);

        Business testBusiness = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
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
        product = productRepository.save(product);

        LocalDate today = LocalDate.now();

        inventoryItem = new InventoryItem.Builder()
                .withProduct(product)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(inventoryItem);

        saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today.plus(1, ChronoUnit.DAYS).toString())
                .withPrice("100")
                .withQuantity(1)
                .build();
        saleItem = saleItemRepository.save(saleItem);
    }

    @AfterEach
    void tearDown() {
        deleteAll();
    }

    @Test
    void findInterestEventByNotifiedUserAndSaleItem_noMatchingEvents_emptyReturned() {
        // Different user, same sale item
        User bystander = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith97@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        bystander = userRepository.save(bystander);
        interestEventRepository.save(new InterestEvent(bystander, saleItem));

        // Different sale item, same user
        SaleItem otherSaleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1, ChronoUnit.DAYS).toString())
                .withPrice("10")
                .withQuantity(2)
                .build();
        otherSaleItem = saleItemRepository.save(otherSaleItem);
        interestEventRepository.save(new InterestEvent(testUser, otherSaleItem));


        var found = interestEventRepository.findInterestEventByNotifiedUserAndSaleItem(testUser, saleItem);
        assertTrue(found.isEmpty());
    }

    @Test
    void findInterestEventByNotifiedUserAndSaleItem_eventMatches_eventReturned() {
        var interestEvent = interestEventRepository.save(new InterestEvent(testUser, saleItem));

        var found = interestEventRepository.findInterestEventByNotifiedUserAndSaleItem(testUser, saleItem);
        assertTrue(found.isPresent());
        assertEquals(interestEvent.getId(), found.get().getId());
        assertEquals(testUser.getUserID(), found.get().getNotifiedUser().getUserID());
        assertEquals(saleItem.getId(), found.get().getSaleItem().getId());
    }
}
