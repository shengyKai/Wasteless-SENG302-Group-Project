package org.seng302.leftovers.tools;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.controllers.DGAAController;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
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

    private void updateTestRepositories() {
        testUser = userRepository.save(testUser);
        testBusiness = businessRepository.save(testBusiness);
        testProduct = productRepository.save(testProduct);
        testInventoryItem = inventoryItemRepository.save(testInventoryItem);
        testSaleItem = saleItemRepository.save(testSaleItem);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "The Nathan Apple:The Nathan Apple",
            "Orange Apple:Orange Apple",
            "Sweet Lime:Sweet Lime",
            "Tangerine:Tangerine",
            "Juicy Fruit:Juicy Fruit",
            "\"Juicy Apple\":Juicy Apple"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesFullProductName_saleItemReturned(String query, String name) {
        testProduct.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getProduct().getName());
    }
    
    @ParameterizedTest
    @CsvSource(value = {
            "Natha:The Nathan Apple",
            "athan:The Nathan Apple",
            "Apple:The Nathan Apple",
            "The:The Nathan Apple",
            "Oran:Orange Apple",
            "A:Juicy Apple"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesPartialProductName_saleItemReturned(String query, String name) {
        testProduct.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getProduct().getName());
    }
    
    @ParameterizedTest
    @CsvSource(value = {
            "wow,much,pog:The Nathan Apple",
            "\"Juicy Apple\":apple", "Orange Apple",
            "yes.yes:Sweet Lime"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingProductName_doesNotMatchProductName_saleItemNotReturned(String query, String name) {
        testProduct.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }
    
    @Transactional
    @ParameterizedTest
    @CsvSource(value = {
            "Nathans pies:Nathans pies",
            "Ellas pies:Ellas pies",
            "Dons pies:Dons pies",
            "Joey Aluminium:Joey Aluminium",
            "Doug Digs:Doug Digs",
            "Wonsons Wontons:Wonsons Wontons",
            "Wonsons Wontons:Wonsons wontons"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesSellersName_saleItemReturned(String query, String name) {
        testBusiness.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getInventoryItem().getProduct().getBusiness().getName());
    }
    
    @Transactional
    @ParameterizedTest
    @CsvSource(value = {
            "Nathan:Nathans pies",
            "Ella:Ellas pies",
            "pies:Dons pies",
            "Alum:Joey Aluminium",
            "Doug D:Doug Digs",
            "Wontons:Wonsons Wontons",
            "Wonsons:Wonsons wontons"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesPartialSellersName_saleItemReturned(String query, String name) {
        testBusiness.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getInventoryItem().getProduct().getBusiness().getName());
    }


    @ParameterizedTest
    @CsvSource(value = {
            "Ella:Nathans pies",
            "Nathans pies:Ellas pies",
            "Got.Him:Dons pies",
            "$lla:Ellas pies",
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersName_doesNotMatchSellersName_saleItemNotReturned(String query, String name) {
        testBusiness.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", query, "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }
    
    @ParameterizedTest
    @CsvSource(value = {
            "Christchurch:Christchurch:Otago:New Zealand",
            "Dunedin:Dunedin:Otago:New Zealand",
            "Wellington:Wellington:Otago:New Zealand",
            "Auckland:Auckland:Otago:New Zealand",
            "Gore:Gore:Otago:New Zealand",
            "Melbourne:Melbourne:Otago:New Zealand",
            "London:London:Otago:New Zealand",
            "Canterbury:Christchurch:Canterbury:New Zealand",
            "Otago:Christchurch:Otago:New Zealand",
            "Otago:Christchurch:Otago:New Zealand",
            "Tasman:Christchurch:Tasman:New Zealand",
            "Marlborough:Christchurch:Marlborough:New Zealand",
            "New Zealand:Christchurch:Otago:New Zealand",
            "Australia:Christchurch:Otago:Australia",
            "Luxembourg:Christchurch:Otago:Luxembourg",
            "England:Christchurch:Otago:England",
            "Great Britain:Christchurch:Otago:Great Britain"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesLocation_saleItemReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(city);
        testBusinessLocation.setRegion(region);
        testBusinessLocation.setCountry(country);
        testBusiness.setAddress(testBusinessLocation);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
    }
    
    @ParameterizedTest
    @CsvSource(value = {
            "Christ:Christchurch:Otago:New Zealand",
            "din:Dunedin:Otago:New Zealand",
            "ton:Wellington:Otago:New Zealand",
            "Auck:Auckland:Otago:New Zealand",
            "e:Gore:Otago:New Zealand",
            "Mel:Melbourne:Otago:New Zealand",
            "don:London:Otago:New Zealand",
            "Can:Christchurch:Canterbury:New Zealand",
            "go:Christchurch:Otago:New Zealand",
            "man:Christchurch:Tasman:New Zealand",
            "borough:Christchurch:Marlborough:New Zealand",
            "New:Christchurch:Otago:New Zealand"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesPartialLocation_saleItemReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(city);
        testBusinessLocation.setRegion(region);
        testBusinessLocation.setCountry(country);
        testBusiness.setAddress(testBusinessLocation);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
    }


    @ParameterizedTest
    @CsvSource(value = {
            "Christ,church:Christchurch:Otago:New Zealand",
            "Dun,edin:Dunedin:Otago:New Zealand",
            "A....uckland:Auckland:Otago:New Zealand",
            "Gor$:Gore:Otago:New Zealand",
            "Gr#at Br!ti@n:Christchurch:Otago:Great Britain"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_doesNotMatchLocation_saleItemNotReturned(String query, String city, String region, String country) {
        testBusinessLocation.setCity(city);
        testBusinessLocation.setRegion(region);
        testBusinessLocation.setCountry(country);
        testBusiness.setAddress(testBusinessLocation);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs pies", "Simple Pie", "Canberra", "NSW", "Australia",
                        "\"Canberra\" and \"NSW\" AND \"Australia\"",
                        "\"Davids pies\" OR \"Gregs pies\"", "\"Taiwan\" or \"Australia\""})
    void constructSaleItemSpecificationUsingOnlySearchQuery_fullMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(testSaleItem.getSaleId(), matches.get(0).getSaleId());
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs", "pies", "Simple", "Pie", "Pi", "Can", "berra", "NS", "W", "Austra", "lia",
                        "\"Gregs\" AND \"pie\"", "\"Can\" AND \"Austra\"",
                        "\"Davids\" or \"Gregs\"", "\"Austral\" or \"Greenland\""})
    void constructSaleItemSpecificationUsingOnlySearchQuery_partialMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(testSaleItem.getSaleId(), matches.get(0).getSaleId());
    }

    @ParameterizedTest
    @ValueSource(strings={"Austra,lia", "Gre.g", "Greenland", "Davids pies", "North America", "#$%"})
    void constructSaleItemSpecificationUsingOnlySearchQuery_doesNotMatchSearchQuery_saleItemNotReturned(String query) {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }
    @Test
    void yeet() {
        Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(
                "Apple AND Carrot", "", "", "");
        testBusiness.setName("Carrot Inc.");
        testProduct.setName("Apple");
        updateTestRepositories();

        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
    }
}
