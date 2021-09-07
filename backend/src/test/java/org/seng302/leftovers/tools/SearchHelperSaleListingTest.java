package org.seng302.leftovers.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.controllers.DGAAController;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchHelperSaleListingTest {

    private User testUser;
    private Business testBusiness;
    private Product testProduct;
    private InventoryItem testInventoryItem;
    private SaleItem testSaleItem;
    private Location testBusinessLocation;

    /**
     * Repository storing user entities.
     */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private DGAAController dgaaController;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;

    @BeforeAll
    void init() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();

        Location userLocation = new Location.Builder()
                .atDistrict("District")
                .atStreetNumber("45")
                .onStreet("Street place")
                .inCountry("Australia")
                .inRegion("NSW")
                .atDistrict("someplace")
                .inCity("Canberra")
                .withPostCode("5011").build();
        testBusinessLocation = new Location.Builder()
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
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(testBusinessLocation)
                .withName("Gregs pies")
                .withDescription("We enjoy pies")
                .withPrimaryOwner(testUser).build();
        testBusiness = businessRepository.save(testBusiness);
        testProduct = new Product.Builder()
                .withName("Simple Pie")
                .withProductCode("GFD432")
                .withDescription("Yummy")
                .withBusiness(testBusiness)
                .withManufacturer("Good pies")
                .withRecommendedRetailPrice("54").build();
        testProduct = productRepository.save(testProduct);
        testInventoryItem = new InventoryItem.Builder()
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
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    private static Stream<Arguments> productNameFull() {
        return Stream.of(
                arguments("The Nathan Apple", "The Nathan Apple"),
                arguments("Orange Apple", "Orange Apple"),
                arguments("Sweet Lime","Sweet Lime"),
                arguments("Tangerine", "Tangerine"),
                arguments("Juicy Fruit", "Juicy Fruit"),
                arguments("\"Juicy Apple\"", "Juicy Apple")
        );
    }
    @ParameterizedTest
    @MethodSource("productNameFull")
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesFullProductName_saleItemReturned(String query, String name) {
        testProduct.setName(name);
        testProduct = productRepository.save(testProduct);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getProduct().getName());
    }

    private static Stream<Arguments> productNamePartial() {
        return Stream.of(
                arguments("Natha", "The Nathan Apple"),
                arguments("athan", "The Nathan Apple"),
                arguments("Apple", "The Nathan Apple"),
                arguments("The", "The Nathan Apple"),
                arguments("Oran", "Orange Apple"),
                arguments("A", "Juicy Apple")
        );
    }
    @ParameterizedTest
    @MethodSource("productNamePartial")
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesPartialProductName_saleItemReturned(String query, String name) {
        testProduct.setName(name);
        testProduct = productRepository.save(testProduct);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getProduct().getName());
    }

    private static Stream<Arguments> productNameInvalid() {
        return Stream.of(
                arguments("wow,much,pog", "The Nathan Apple"),
                arguments("\"Juicy Apple\", apple", "Orange Apple"),
                arguments("yes.yes","Sweet Lime")
        );
    }
    @ParameterizedTest
    @MethodSource("productNameInvalid")
    void constructSaleItemSpecificationOnlyIncludingProductName_doesNotMatchProductName_saleItemNotReturned(String query, String name) {
        testProduct.setName(name);
        testProduct = productRepository.save(testProduct);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    private static Stream<Arguments> businessNameFull() {
        return Stream.of(
                arguments("Nathans pies", "Nathans pies"),
                arguments("Ellas pies", "Ellas pies"),
                arguments("Dons pies", "Dons pies"),
                arguments("Joey Aluminium", "Joey Aluminium"),
                arguments("Doug Digs", "Doug Digs"),
                arguments("Wonsons Wontons", "Wonsons Wontons"),
                arguments("Wonsons Wontons", "Wonsons wontons")
        );
    }
    @ParameterizedTest
    @MethodSource("businessNameFull")
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesSellersName_saleItemReturned(String query, String name) {
        testBusiness.setName(name);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getBusiness().getName());
    }
    private static Stream<Arguments> businessNamePartial() {
        return Stream.of(
                arguments("Nathan", "Nathans pies"),
                arguments("Ella", "Ellas pies"),
                arguments("pies", "Dons pies"),
                arguments("Alum", "Joey Aluminium"),
                arguments("Doug D", "Doug Digs"),
                arguments("Wontons", "Wonsons Wontons"),
                arguments("Wonsons", "Wonsons wontons")
        );
    }
    @ParameterizedTest
    @MethodSource("businessNamePartial")
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesPartialSellersName_saleItemReturned(String query, String name) {
        testBusiness.setName(name);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getBusiness().getName());
    }

    private static Stream<Arguments> businessNameInvalid() {
        return Stream.of(
                arguments("Ella", "Nathans pies"),
                arguments("Nathans pies", "Ellas pies"),
                arguments("Got.Him", "Dons pies"),
                arguments("$lla", "Ellas pies")

        );
    }
    @ParameterizedTest
    @MethodSource("businessNameInvalid")
    void constructSaleItemSpecificationOnlyIncludingSellersName_doesNotMatchSellersName_saleItemNotReturned(String query, String name) {
        testBusiness.setName(name);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    private static Stream<Arguments> locationFull() {
        return Stream.of(
                arguments("Christchurch", "Christchurch", "Otago", "New Zealand"),
                arguments("Dunedin", "Dunedin", "Otago", "New Zealand"),
                arguments("Wellington", "Wellington", "Otago", "New Zealand"),
                arguments("Auckland", "Auckland", "Otago", "New Zealand"),
                arguments("Gore", "Gore", "Otago", "New Zealand"),
                arguments("Melbourne", "Melbourne", "Otago", "New Zealand"),
                arguments("London", "London", "Otago", "New Zealand"),
                arguments("Canterbury", "Christchurch", "Canterbury", "New Zealand"),
                arguments("Otago", "Christchurch", "Otago", "New Zealand"),
                arguments("Tasman", "Christchurch", "Tasman", "New Zealand"),
                arguments("Marlborough", "Christchurch", "Marlborough", "New Zealand"),
                arguments("New Zealand", "Christchurch", "Otago", "New Zealand"),
                arguments("Australia", "Christchurch", "Otago", "Australia"),
                arguments("Luxembourg", "Christchurch", "Otago", "Luxembourg"),
                arguments("England", "Christchurch", "Otago", "England"),
                arguments("Great Britain", "Christchurch", "Otago", "Great Britain")
        );
    }
    @ParameterizedTest
    @MethodSource("locationFull")
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesLocation_saleItemReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(query);
        testBusinessLocation.setRegion(query);
        testBusinessLocation.setCountry(query);
        testBusiness.setAddress(testBusinessLocation);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    private static Stream<Arguments> locationPartial() {
        return Stream.of(
                arguments("Christ", "Christchurch", "Otago", "New Zealand"),
                arguments("din", "Dunedin", "Otago", "New Zealand"),
                arguments("ton", "Wellington", "Otago", "New Zealand"),
                arguments("Auck", "Auckland", "Otago", "New Zealand"),
                arguments("e", "Gore", "Otago", "New Zealand"),
                arguments("Mel", "Melbourne", "Otago", "New Zealand"),
                arguments("don", "London", "Otago", "New Zealand"),
                arguments("Can", "Christchurch", "Canterbury", "New Zealand"),
                arguments("go", "Christchurch", "Otago", "New Zealand"),
                arguments("man", "Christchurch", "Tasman", "New Zealand"),
                arguments("borough", "Christchurch", "Marlborough", "New Zealand"),
                arguments("New", "Christchurch", "Otago", "New Zealand")
        );
    }
    @ParameterizedTest
    @MethodSource("locationPartial")
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesPartialLocation_saleItemReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(query);
        testBusinessLocation.setRegion(query);
        testBusinessLocation.setCountry(query);
        testBusiness.setAddress(testBusinessLocation);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    private static Stream<Arguments> locationInvalid() {
        return Stream.of(
                arguments("Christ,church", "Christchurch", "Otago", "New Zealand"),
                arguments("Dun,edin", "Dunedin", "Otago", "New Zealand"),
                arguments("A....uckland", "Auckland", "Otago", "New Zealand"),
                arguments("Gor$", "Gore", "Otago", "New Zealand"),
                arguments("Gr#at Br!ti@n", "Christchurch", "Otago", "Great Britain")
                );
    }
    @ParameterizedTest
    @MethodSource("locationInvalid")
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_doesNotMatchLocation_saleItemNotReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(query);
        testBusinessLocation.setRegion(query);
        testBusinessLocation.setCountry(query);
        testBusiness.setAddress(testBusinessLocation);
        testBusiness = businessRepository.save(testBusiness);
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs pies", "Simple Pie", "Canberra", "NSW", "Australia",
                        "\"Gregs pies\" AND \"Simple Pie\"", "\"Canberra\" and \"NSW\" AND \"Australia\"",
                        "\"Davids pies\" OR \"Gregs pies\"", "\"Taiwan\" or \"Australia\""})
    void constructSaleItemSpecificationUsingOnlySearchQuery_fullMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(matches.get(0), testSaleItem);
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs", "pies", "Simple", "Pie", "Pi", "Can", "berra", "NS", "W", "Austra", "lia",
                        "\"Gregs\" AND \"pie\"", "\"Can\" AND \"Austra\"", "\"W\" AND \"pie\"",
                        "\"Davids\" or \"Gregs\"", "\"Austral\" or \"Greenland\""})
    void constructSaleItemSpecificationUsingOnlySearchQuery_partialMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(matches.get(0), testSaleItem);
    }

    @ParameterizedTest
    @ValueSource(strings={"Austra,lia", "Gre.g", "Greenland", "Davids pies", "North America", "#$%"})
    void constructSaleItemSpecificationUsingOnlySearchQuery_doesNotMatchSearchQuery_saleItemNotReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }
}
