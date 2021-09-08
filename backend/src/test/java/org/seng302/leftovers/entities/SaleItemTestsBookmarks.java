package org.seng302.leftovers.entities;

import org.junit.jupiter.api.BeforeAll;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
                .withEmail("johnsmith98@gmail.com")
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
                .withEmail("johnsmith98@gmail.com")
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

    void addBookmarkToSaleListing_oneUserBookmarks_userHasBookmarkedSaleListing() {
        //TODO
    }

    void addBookmarkToSaleListing_twoUserBookmarks_bothUsersHaveBookmarkedSaleListing() {
        //TODO
    }

    void addBookmarkToSaleListing_threeUserBookmarks_allUsersHaveBookmarkedSaleListing() {
        //TODO
    }

    void addBookmarkToSaleListing_userBookmarksAnAlreadyBookmarkedSaleListing_userHasNotBookmarkedSaleListingTwice() {
        //TODO
    }

    void removeBookmarkFromSaleListing_userRemovesBookmarkFromSaleListing_userHasNoBookmarkWithSaleListing() {
        //TODO
    }

    void removeBookmarkFromSaleListing_twoUsersRemovesBookmarkFromSaleListing_bothUsersHaveNoBookmarkWithSaleListing() {
        //TODO
    }

    void removeBookmarkFromSaleListing_threeUsersRemovesBookmarkFromSaleListing_allUsersHaveNoBookmarkWithSaleListing() {
        //TODO
    }

    void removeBookmarkFromSaleListing_userRemovesBookmarkFromUnbookmarkedSaleListing_nothingChangesExceptionThrown() {
        //TODO
    }

    void addBookmarkToSaleListing_userTriesToBookmarkTwoSaleListing_onlyOneSaleListingIsBookmarked() {
        //TODO
    }

    void addRemoveAddBookmarkToSaleListing_userAddsRemovesAndReaddsBookmarkToSaleListing_userHasBookmarkedSaleListing() {
        //TODO
    }
}
