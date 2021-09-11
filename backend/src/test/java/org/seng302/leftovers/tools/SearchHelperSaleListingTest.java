package org.seng302.leftovers.tools;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.controllers.DGAAController;
import org.seng302.leftovers.dto.SaleListingSearchDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.service.searchservice.SearchPageConstructor;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchHelperSaleListingTest {

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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
                "", query, "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        SaleItem saleItem = matches.get(0);
        assertEquals(name, saleItem.getProduct().getName());
    }
    
    @ParameterizedTest
    @CsvSource(value = {
            "wow,much,pog:The Nathan Apple",
            "Massive orange:apple",
            "yes.yes:Sweet Lime"
    }, delimiter = ':')
    void constructSaleItemSpecificationOnlyIncludingProductName_doesNotMatchProductName_saleItemNotReturned(String query, String name) {
        testProduct.setName(name);
        updateTestRepositories();
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
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
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
                "", "", "", query);
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs pies", "Simple Pie", "Canberra", "NSW", "Australia",
                        "\"Canberra\" and \"NSW\" AND \"Australia\"",
                        "\"Davids pies\" OR \"Gregs pies\"", "\"Taiwan\" or \"Australia\"", "good pies", "Yummy"})
    void constructSaleItemSpecificationUsingOnlySearchQuery_fullMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(testSaleItem.getId(), matches.get(0).getId());
    }

    @ParameterizedTest
    @ValueSource(strings={"Gregs", "pies", "Simple", "Pie", "Pi", "Can", "berra", "NS", "W", "Austra", "lia",
                        "\"Gregs\" AND \"pie\"", "\"Can\" AND \"Austra\"",
                        "\"Davids\" or \"Gregs\"", "\"Austral\" or \"Greenland\"", "good", "yum"})
    void constructSaleItemSpecificationUsingOnlySearchQuery_partialMatchSearchQuery_saleItemReturned(String query) {
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals(testSaleItem.getId(), matches.get(0).getId());
    }

    @ParameterizedTest
    @ValueSource(strings={"Austra,lia", "Gre.g", "Greenland", "Davids pies", "North America", "#$%"})
    void constructSaleItemSpecificationUsingOnlySearchQuery_doesNotMatchSearchQuery_saleItemNotReturned(String query) {
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(
                query, "", "", "");
        List<SaleItem> matches = saleItemRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Transactional
    protected Business createBusiness() {
        var testUser = userRepository.findAll().iterator().next();
        var testBusiness = new Business.Builder()
                .withPrimaryOwner(testUser)
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();

        return businessRepository.save(testBusiness);
    }

    /**
     * This method is for setting up sale items with sale items of different prices and closing dates
     */
    private void setUpSaleItemsWithDifferentPricesClosingDates() {
        var business = createBusiness();
        LocalDate today = LocalDate.now();
        var product = new Product.Builder()
                .withBusiness(business)
                .withProductCode("PRODUCT-CODE")
                .withName("This is the product name")
                .withDescription("Wow description")
                .withManufacturer("Some guy")
                .build();
        productRepository.save(product);
        var inventoryItem = new InventoryItem.Builder()
                .withProduct(product)
                .withQuantity(30)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItem = inventoryItemRepository.save(inventoryItem);
        var saleItem1 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(1)
                .withPrice("1.0")
                .withMoreInfo("blah")
                .withCloses(today.plus(1, ChronoUnit.DAYS).toString())
                .build();
        saleItemRepository.save(saleItem1);
        var saleItem2 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(1)
                .withPrice("6.0")
                .withMoreInfo("blah")
                .withCloses(today.plus(6, ChronoUnit.DAYS).toString())
                .build();
        saleItemRepository.save(saleItem2);
        var saleItem3 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(1)
                .withPrice("15.0")
                .withMoreInfo("blah")
                .withCloses(today.plus(15, ChronoUnit.DAYS).toString())
                .build();
        saleItemRepository.save(saleItem3);
    }

    @ParameterizedTest
    @CsvSource({"-1.0,0,0", "0.0,5.0,1", "6.0,10.0,1", "10.0,15.0,1", "0.0,15.0,3", "0.0,,3", ",15.0,3", "3.0,,2", ",12.0,2", ",,3"})
    void constructSaleListingSpecificationFromPrice_saleItemsCreatedWithDifferentPrices_saleItemsReturnedAreWithinRange(String priceLowerBound, String priceUpperBound, String expectedSize) throws Exception {
        // This line is required because the set up above is affecting the results of the test cases below
        saleItemRepository.deleteAll();
        setUpSaleItemsWithDifferentPricesClosingDates();

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(1, 10, Sort.by("created"));

        BigDecimal lowerBound = null;
        BigDecimal upperBound = null;
        if (priceLowerBound != null) {
            lowerBound = new BigDecimal(priceLowerBound);
        }
        if (priceUpperBound != null) {
            upperBound = new BigDecimal(priceUpperBound);
        }
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleListingSpecificationFromPrice(lowerBound, upperBound);
        Page<SaleItem> resultSaleItemsBusiness = saleItemRepository.findAll(specification, pageRequest);

        assertEquals(Integer.parseInt(expectedSize), resultSaleItemsBusiness.getTotalElements());

        for (SaleItem saleItem : resultSaleItemsBusiness) {
            // This would mean the saleItem price is being compared to the lowerBound such that its equal or more than the lowerBound
            if (lowerBound != null) {
                assertTrue(saleItem.getPrice().compareTo(lowerBound) >= 0);
            }
            // This would mean the saleItem price is being compared to the lowerBound such that its equal or lesser than the upperBound
            if (upperBound != null) {
                assertTrue(saleItem.getPrice().compareTo(upperBound) <= 0);
            }

        }
    }

    @ParameterizedTest
    @CsvSource({"-1,0,0", "0,5,1", "6,10,1", "10,15,1", "0,15,3", ",15,3", "0,,3", ",12,2", "3,,2", ",,3"})
    void constructSaleListingSpecificationFromClosingDate_saleItemsCreatedWithDifferentClosingDates_saleItemsReturnedAreWithinRange(String dateLowerBoundToAdd, String dateUpperBoundToAdd, String expectedSize) throws Exception {
        // This line is required because the set up above is affecting the results of the test cases below
        saleItemRepository.deleteAll();
        setUpSaleItemsWithDifferentPricesClosingDates();

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(1, 10, Sort.by("created"));

        LocalDate lowerBound = null;
        if (dateLowerBoundToAdd != null) {
            lowerBound = LocalDate.now().plus(Integer.parseInt(dateLowerBoundToAdd), ChronoUnit.DAYS);
        }
        LocalDate upperBound = null;
        if (dateUpperBoundToAdd != null) {
            upperBound = LocalDate.now().plus(Integer.parseInt(dateUpperBoundToAdd), ChronoUnit.DAYS);
        }
        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleListingSpecificationFromClosingDate(lowerBound, upperBound);
        Page<SaleItem> resultSaleItemsBusiness = saleItemRepository.findAll(specification, pageRequest);

        assertEquals(Integer.parseInt(expectedSize), resultSaleItemsBusiness.getTotalElements());

        for (SaleItem saleItem : resultSaleItemsBusiness) {
            // This would mean the saleItem closing date is being compared to the lowerBound such that its equal or more than the lowerBound
            if (dateLowerBoundToAdd != null) {
                assertTrue(saleItem.getCloses().compareTo(lowerBound) >= 0);
            }
            // This would mean the saleItem closing date is being compared to the lowerBound such that its equal or lesser than the upperBound
            if (dateUpperBoundToAdd != null) {
                assertTrue(saleItem.getCloses().compareTo(upperBound) <= 0);
            }

        }
    }

    /**
     * This method is for setting up sale items with businesses of different business types when given a business type
     * @param businessType to set up the business with
     */
    @Transactional
    protected void setUpSaleItemsWithDifferentBusinessTypes(String businessType) {
        var testUser = userRepository.findAll().iterator().next();
        var testBusiness = new Business.Builder()
                .withPrimaryOwner(testUser)
                .withBusinessType(businessType)
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        businessRepository.save(testBusiness);

        LocalDate today = LocalDate.now();
        var product = new Product.Builder()
                .withBusiness(testBusiness)
                .withProductCode("PRODUCT-CODE")
                .withName("This is the product name")
                .withDescription("Wow description")
                .withManufacturer("Some guy")
                .build();
        productRepository.save(product);
        var inventoryItem = new InventoryItem.Builder()
                .withProduct(product)
                .withQuantity(30)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItem = inventoryItemRepository.save(inventoryItem);
        var saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(1)
                .withPrice("3.0")
                .withMoreInfo("blah")
                .withCloses(today.plus(11, ChronoUnit.DAYS).toString())
                .build();
        saleItemRepository.save(saleItem);
    }

    /**
     * This method is solely for generating the arguments for the test:
     * constructSaleListingSpecificationFromBusinessType_businessesCreatedWithDifferentBusinessTypes_saleItemsReturnedAreFromThatBusinessType
     * @return a stream containing the arguments for the test method
     */
    private Stream<Arguments> generateDataForConstructSaleListingSpecificationFromBusinessType() {
        return Stream.of(
                Arguments.of(Arrays.asList(), 4),
                Arguments.of(Arrays.asList("Accommodation and Food Services"), 1),
                Arguments.of(Arrays.asList("Accommodation and Food Services", "Charitable organisation"), 2),
                Arguments.of(Arrays.asList("Accommodation and Food Services", "Charitable organisation", "Non-profit organisation"), 3),
                Arguments.of(Arrays.asList("Accommodation and Food Services", "Charitable organisation", "Non-profit organisation", "Retail Trade"), 4)
        );
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("generateDataForConstructSaleListingSpecificationFromBusinessType")
    void constructSaleListingSpecificationFromBusinessType_businessesCreatedWithDifferentBusinessTypes_saleItemsReturnedAreFromThatBusinessType(List<String> expectedBusinessTypes, int expectedSize) throws Exception {
        // This line is required because the set up above is affecting the results of the test cases below
        saleItemRepository.deleteAll();
        for (String businessType : Business.getBusinessTypes()) {
            setUpSaleItemsWithDifferentBusinessTypes(businessType);
        }

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(1, 10, Sort.by("created"));

        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleListingSpecificationFromBusinessType(expectedBusinessTypes);
        Page<SaleItem> resultSaleItemsBusiness = saleItemRepository.findAll(specification, pageRequest);

        assertEquals(expectedSize, resultSaleItemsBusiness.getTotalElements());

        if (expectedBusinessTypes.size() > 0) {
            for (SaleItem saleItem : resultSaleItemsBusiness) {
                assertTrue(expectedBusinessTypes.contains(saleItem.getInventoryItem().getBusiness().getBusinessType()));
            }
        }
    }

    /**
     * This method is solely for generating the arguments for the test:
     * constructSaleListingSpecificationForSearch_differentPricesClosingDatesBusinessTypes_saleItemsReturnedAreOfTheSpecification
     * @return a stream containing the arguments for the test method
     */
    private Stream<Arguments> generateDataForconstructSaleListingSpecificationForSearch() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("0.0"), new BigDecimal("0.0"),
                        today.plus(Integer.parseInt("0"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("0"), ChronoUnit.DAYS),
                        Arrays.asList()), 0),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("0.0"), new BigDecimal("5.0"),
                        today.plus(Integer.parseInt("0"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("5"), ChronoUnit.DAYS),
                        Arrays.asList("Accommodation and Food Services")), 1),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("6.0"), new BigDecimal("10.0"),
                        today.plus(Integer.parseInt("6"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("10"), ChronoUnit.DAYS),
                        Arrays.asList("Accommodation and Food Services", "Charitable organisation")), 1),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("11.0"), new BigDecimal("15.0"),
                        today.plus(Integer.parseInt("11"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("15"), ChronoUnit.DAYS),
                        Arrays.asList("Accommodation and Food Services", "Charitable organisation", "Non-profit organisation")), 1),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("0.0"), new BigDecimal("15.0"),
                        today.plus(Integer.parseInt("0"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("15"), ChronoUnit.DAYS),
                        Arrays.asList("Accommodation and Food Services", "Charitable organisation", "Non-profit organisation", "Retail Trade")), 7),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("0.0"), new BigDecimal("14.0"),
                        today.plus(Integer.parseInt("2"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("15"), ChronoUnit.DAYS),
                        Arrays.asList("Accommodation and Food Services", "Charitable organisation")), 3),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("2.0"), new BigDecimal("14.0"),
                        today.plus(Integer.parseInt("3"), ChronoUnit.DAYS),
                        today.plus(Integer.parseInt("12"), ChronoUnit.DAYS),
                        Arrays.asList()), 5),
                Arguments.of(new SaleListingSearchDTO(new BigDecimal("2.0"), null,
                        today.plus(Integer.parseInt("3"), ChronoUnit.DAYS),
                        null,
                        Arrays.asList()), 6),
                Arguments.of(new SaleListingSearchDTO(null, new BigDecimal("14.0"),
                        null, today.plus(Integer.parseInt("12"), ChronoUnit.DAYS),
                        Arrays.asList()), 6),
                Arguments.of(new SaleListingSearchDTO(null, null,
                        null, null,
                        Arrays.asList()), 7)
        );
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("generateDataForconstructSaleListingSpecificationForSearch")
    void constructSaleListingSpecificationForSearch_differentPricesClosingDatesBusinessTypes_saleItemsReturnedAreOfTheSpecification(SaleListingSearchDTO saleListingSearchDTO, int expectedSize) {
        // This line is required because the set up above is affecting the results of the test cases below
        saleItemRepository.deleteAll();
        // Make use of the two methods for generating sale items and use them as test cases
        for (String businessType : Business.getBusinessTypes()) {
            setUpSaleItemsWithDifferentBusinessTypes(businessType);
        }
        setUpSaleItemsWithDifferentPricesClosingDates();

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(1, 10, Sort.by("created"));

        Specification<SaleItem> specification = SearchSpecConstructor.constructSaleListingSpecificationForSearch(saleListingSearchDTO);
        Page<SaleItem> resultSaleItemsBusiness = saleItemRepository.findAll(specification, pageRequest);

        assertEquals(expectedSize, resultSaleItemsBusiness.getTotalElements());
    }
}
