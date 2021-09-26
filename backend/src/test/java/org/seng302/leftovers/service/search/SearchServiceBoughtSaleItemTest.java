package org.seng302.leftovers.service.search;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BoughtSaleItemRepository;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchServiceBoughtSaleItemTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BoughtSaleItemRepository boughtSaleItemRepository;

    private User owner;
    private Business business;

    private final Instant referenceInstant = Instant.parse("2021-09-08T08:47:59Z");

    @BeforeAll
    void init() {
        boughtSaleItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        owner = userRepository.save(owner);
        User buyer = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("davesmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        buyer = userRepository.save(buyer);
        business = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Joe's Garage")
                .withPrimaryOwner(owner)
                .build();
        business = businessRepository.save(business);

        var product1 = new Product.Builder()
                .withBusiness(business)
                .withProductCode("TEST-1")
                .withName("test_product")
                .build();
        product1 = productRepository.save(product1);

        var product2 = new Product.Builder()
                .withBusiness(business)
                .withProductCode("TEST-2")
                .withName("test_product2")
                .build();
        product2 = productRepository.save(product2);

        boughtSaleItemRepository.save(createBoughtSaleItem(product1, buyer, referenceInstant));
        boughtSaleItemRepository.save(createBoughtSaleItem(product2, buyer, referenceInstant.plus(1, ChronoUnit.DAYS)));
        boughtSaleItemRepository.save(createBoughtSaleItem(product1, owner, referenceInstant.plus(2, ChronoUnit.DAYS)));
    }

    @SneakyThrows
    private BoughtSaleItem createBoughtSaleItem(Product product, User buyer, Instant saleDate) {
        var saleItem = mock(SaleItem.class);
        when(saleItem.getPrice()).thenReturn(new BigDecimal("1.0"));
        when(saleItem.getProduct()).thenReturn(product);
        when(saleItem.getCreated()).thenReturn(Instant.now());

        var boughtSaleItem = new BoughtSaleItem(saleItem, buyer);

        Field saleDateField = BoughtSaleItem.class.getDeclaredField("saleDate");
        saleDateField.setAccessible(true);
        saleDateField.set(boughtSaleItem, saleDate);

        return boughtSaleItem;
    }

    @AfterEach
    void tearDown() {
        boughtSaleItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            ",,3", // No start or end, all should be returned
            "0,0,0", // Empty range, none should match
            "4,5,0", // No bought sale items in range, none should match
            "1,,2", // Only first bought sale item should be filtered out
            ",1,1", // Only first should match
            "0,2,2", // Last should not match
    })
    void constructFromPeriod_variousDateRangesUsingOffset_expectedNumberReturned(Integer lowerOffset, Integer upperOffset, int expectedCount) {
        var start = Optional.ofNullable(lowerOffset).map(offset -> referenceInstant.plus(offset, ChronoUnit.DAYS)).orElse(null);
        var end = Optional.ofNullable(upperOffset).map(offset -> referenceInstant.plus(offset, ChronoUnit.DAYS)).orElse(null);

        var spec = SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(start, end);
        var actualCount = boughtSaleItemRepository.count(spec);

        assertEquals(expectedCount, actualCount);
    }

    @Test
    void constructFromPeriod_rangeMatchesOne_expectedOneReturned() {
        var start = referenceInstant.plus(16, ChronoUnit.HOURS);
        var end = referenceInstant.plus(32, ChronoUnit.HOURS);

        var spec = SearchSpecConstructor.constructBoughtSaleListingSpecificationFromPeriod(start, end);

        var result = boughtSaleItemRepository.findOne(spec);
        assertTrue(result.isPresent());
        assertEquals(referenceInstant.plus(1, ChronoUnit.DAYS), result.get().getSaleDate());
    }

    @Test
    void constructFromBusiness_noMatchingBoughtSaleItems_noneReturned() {
        Business emptyBusiness = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Joe's Garage")
                .withPrimaryOwner(owner)
                .build();
        emptyBusiness = businessRepository.save(emptyBusiness);

        var spec = SearchSpecConstructor.constructBoughtSaleListingSpecificationFromBusiness(emptyBusiness);
        assertEquals(0, boughtSaleItemRepository.count(spec));
    }

    @Test
    void constructFromBusiness_withMatching_expectedNumberReturned() {
        var spec = SearchSpecConstructor.constructBoughtSaleListingSpecificationFromBusiness(business);
        assertEquals(3, boughtSaleItemRepository.count(spec));
    }
}
