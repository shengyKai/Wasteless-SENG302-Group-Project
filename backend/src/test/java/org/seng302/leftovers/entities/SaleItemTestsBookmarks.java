package org.seng302.leftovers.entities;

import org.junit.jupiter.api.*;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaleItemTestsBookmarks {
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

    User testUser1;
    User testUser2;
    User testUser3;
    Business testBusiness;
    Product testProduct;
    InventoryItem testInvItem;
    SaleItem testSaleItem1;
    SaleItem testSaleItem2;

    @BeforeAll
    void initialise() {
        testUser1 = new User.Builder()
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
        testUser2 = new User.Builder()
                .withFirstName("Henry")
                .withMiddleName("Hector")
                .withLastName("James")
                .withNickName("Jonny")
                .withEmail("johnsmith96@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser3 = new User.Builder()
                .withFirstName("Sapphire")
                .withMiddleName("Hector")
                .withLastName("Charlie")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName1")
                .withPrimaryOwner(testUser1)
                .build();
        businessRepository.save(testBusiness);
        testProduct = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness)
                .build();
        productRepository.save(testProduct);
        LocalDate today = LocalDate.now();
        testInvItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(30)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(testInvItem);

        testSaleItem1 = new SaleItem.Builder()
                .withInventoryItem(testInvItem)
                .withMoreInfo("The holy Nathan Apple")
                .withPrice("10.69")
                .withQuantity(7)
                .build();
        testSaleItem2 = new SaleItem.Builder()
                .withInventoryItem(testInvItem)
                .withMoreInfo("The unholy Nathan Apple")
                .withPrice("69.10")
                .withQuantity(13)
                .build();
        saleItemRepository.save(testSaleItem1);
        saleItemRepository.save(testSaleItem2);
    }

    @AfterEach
    void tearDown() {
        testSaleItem1.removeAllBookmarks();
        testSaleItem2.removeAllBookmarks();
        saleItemRepository.save(testSaleItem1);
        saleItemRepository.save(testSaleItem2);
    }

    @AfterAll
    void cleanUp() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    @Test
    void addBookmarkToSaleListing_oneUserBookmarks_userHasBookmarkedSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(1, bookmarks.size());
        assertEquals(testUser1.getUserID(), bookmarks.get(0).getUserID());
    }

    @Transactional
    @Test
    void addBookmarkToSaleListing_twoUserBookmarks_bothUsersHaveBookmarkedSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        testSaleItem1.addBookmark(testUser2);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(2, bookmarks.size());
        assertEquals(testUser1.getUserID(), bookmarks.get(0).getUserID());
        assertEquals(testUser2.getUserID(), bookmarks.get(1).getUserID());
    }

    @Transactional
    @Test
    void addBookmarkToSaleListing_threeUserBookmarks_allUsersHaveBookmarkedSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        testSaleItem1.addBookmark(testUser2);
        testSaleItem1.addBookmark(testUser3);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(3, bookmarks.size());
        assertEquals(testUser1.getUserID(), bookmarks.get(0).getUserID());
        assertEquals(testUser2.getUserID(), bookmarks.get(1).getUserID());
        assertEquals(testUser3.getUserID(), bookmarks.get(2).getUserID());
    }

    @Transactional
    @Test
    void addBookmarkToSaleListing_userBookmarksAnAlreadyBookmarkedSaleListing_userHasNotBookmarkedSaleListingTwice() {
        testSaleItem1.addBookmark(testUser1);
        saleItemRepository.save(testSaleItem1);
        assertThrows(ResponseStatusException.class, () -> testSaleItem1.addBookmark(testUser1));
    }

    @Transactional
    @Test
    void removeBookmarkFromSaleListing_userRemovesBookmarkFromSaleListing_userHasNoBookmarkWithSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        saleItemRepository.save(testSaleItem1);
        testSaleItem1.removeBookmark(testUser1);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(0, bookmarks.size());
    }

    @Transactional
    @Test
    void removeBookmarkFromSaleListing_twoUsersRemovesBookmarkFromSaleListing_bothUsersHaveNoBookmarkWithSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        testSaleItem1.addBookmark(testUser2);
        saleItemRepository.save(testSaleItem1);
        testSaleItem1.removeBookmark(testUser1);
        testSaleItem1.removeBookmark(testUser2);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(0, bookmarks.size());
    }

    @Transactional
    @Test
    void removeBookmarkFromSaleListing_threeUsersRemovesBookmarkFromSaleListing_allUsersHaveNoBookmarkWithSaleListing() {
        testSaleItem1.addBookmark(testUser1);
        testSaleItem1.addBookmark(testUser2);
        testSaleItem1.addBookmark(testUser3);
        saleItemRepository.save(testSaleItem1);
        testSaleItem1.removeBookmark(testUser1);
        testSaleItem1.removeBookmark(testUser2);
        testSaleItem1.removeBookmark(testUser3);
        saleItemRepository.save(testSaleItem1);
        Optional<SaleItem> saleItemsOptional = saleItemRepository.findById(testSaleItem1.getSaleId());
        assertTrue(saleItemsOptional.isPresent());
        List<User> bookmarks = saleItemsOptional.get().getBookmarks();
        assertEquals(0, bookmarks.size());
    }

    @Transactional
    @Test
    void removeBookmarkFromSaleListing_userRemovesBookmarkFromUnbookmarkedSaleListing_nothingChangesExceptionThrown() {
        assertThrows(ResponseStatusException.class, () -> testSaleItem1.removeBookmark(testUser1));
    }

    @Transactional
    @Test
    void addBookmarkToSaleListing_userTriesToBookmarkTwoSaleListing_onlyOneSaleListingIsBookmarked() {
        //TODO
    }

    @Transactional
    @Test
    void addRemoveAddBookmarkToSaleListing_userAddsRemovesAndReaddsBookmarkToSaleListing_userHasBookmarkedSaleListing() {
        //TODO
    }
}
