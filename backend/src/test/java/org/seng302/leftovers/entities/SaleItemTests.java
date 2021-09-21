package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.dto.saleitem.SaleItemResponseDTO;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.service.searchservice.SearchPageConstructor;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaleItemTests {

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
    private SessionFactory sessionFactory;
    @Autowired
    private ObjectMapper objectMapper;

    private Business testBusiness;
    private Product testProduct;
    private InventoryItem inventoryItem;
    private PageRequest templatePageRequest;
    private User testUser;


    void createTestObjects() throws Exception {
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

        testBusiness = new Business.Builder()
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
        testProduct = productRepository.save(product);

        LocalDate today = LocalDate.now();

        inventoryItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(inventoryItem);
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
    void initialise() {
        Sort.Order expectedOrder = new Sort.Order(Sort.Direction.ASC, "price").ignoreCase();
        templatePageRequest = SearchPageConstructor.getPageRequest(null,null, Sort.by(expectedOrder));
        clearDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        createTestObjects();
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    void createSaleItem_AllFieldsCorrect_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getId()));
    }

    @Test
    void createSaleItem_OnlyCompulsaryFieldsFilled_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(2)
                .withPrice("200.34")
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getId()));
    }

    @Test
    void createSaleItem_ClosesSetToday_ObjectCreated() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withMoreInfo("This expires really soon")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getId()));
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    @Test
    void createSaleItem_ClosesSetYesterday_ObjectNotCreated() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = formatter.format(yesterday());
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(yesterday)
                .withMoreInfo("This has already closed")
                .withPrice("200.34")
                .withQuantity(2);

        var exception = assertThrows(ValidationResponseException.class, builder::build);
        assertEquals("You cannot set close dates in the past", exception.getMessage());
    }

    @Test
    void createSaleItem_NoInventoryItem_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                    .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("200.34")
                    .withQuantity(2);
        var exception = assertThrows(ValidationResponseException.class, builder::build);
        assertEquals("Cannot sell something that is not in your inventory", exception.getMessage());
    }

    @Test
    void createSaleItem_SalePriceNegative_ObjectNotCreated() throws Exception {
        inventoryItem.setPricePerItem(null);
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("-200.34")
                .withQuantity(2);
        assertThrows(ValidationResponseException.class, saleItem::build);
    }

    @Test
    void createSaleItem_SalePriceNull_ObjectNotCreated() throws Exception {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice((BigDecimal) null)
                .withQuantity(2);
        assertThrows(ValidationResponseException.class, saleItem::build);
    }

    @Test
    void createSaleItem_SalePriceUnexpectedInput_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2);

        var exception = assertThrows(ValidationResponseException.class, () -> builder.withPrice("three dollars"));
        assertEquals("The price is not a number", exception.getMessage());
    }

    @Test
    void createSaleItem_QuantityNull_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("3.57");

        var exception = assertThrows(ValidationResponseException.class, builder::build);
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void createSaleItem_QuantityZero_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(0)
                .withPrice("3.57");
        var exception = assertThrows(ValidationResponseException.class, builder::build);
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void createSaleItem_QuantityGreaterThanInventoryTotal_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2000)
                .withPrice("3.57");
        var exception = assertThrows(ValidationResponseException.class, builder::build);
        assertEquals("Cannot sell more items than you have", exception.getMessage());
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToInventoryTotal_ObjectsCreated() throws Exception {
        inventoryItem.setQuantity(10);
        inventoryItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        SaleItem saleItem2 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem2);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getId()));
        Assertions.assertNotNull(saleItemRepository.findById(saleItem2.getId()));
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToGreaterThanInventoryTotal_LastObjectNotCreated() throws Exception {
        inventoryItem.setQuantity(10);
        inventoryItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getId()));
        SaleItem.Builder saleItem2 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(6)
                .withPrice("3.57");
        assertThrows(ValidationResponseException.class, saleItem2::build);
    }

    @Test
    void deleteInventoryItem_SaleItemDeleted() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);

        inventoryItemRepository.deleteAll();
        Optional<SaleItem> foundItem = saleItemRepository.findById(saleItem.getId());
        if (foundItem.isPresent()) { Assertions.fail(); }
    }

    @Test
    void editSaleItem_QuantityStillWithinLimits_SaleItemAndInventoryItemQuantitiesUpdated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        saleItem.setQuantity(3);
        assertEquals(3, saleItem.getQuantity());
        assertEquals(0, inventoryItem.getRemainingQuantity());
    }

    @Test
    void editSaleItem_QuantityGreaterThanInventoryAvailable_NotUpdated() {
        var saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                    .withMoreInfo("This doesn't expire for a long time")
                    .withQuantity(2)
                    .withPrice("3.57")
                    .build();
        saleItemRepository.save(saleItem);
        var exception = assertThrows(ValidationResponseException.class, () -> saleItem.setQuantity(5));
        assertEquals("Cannot sell more items than you have", exception.getMessage());
    }

    @Test
    void createSaleItem_MoreInfoTooLong_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. ")
                .withQuantity(2);
        assertThrows(ValidationResponseException.class, saleItem::build);
    }

    @Test
    void createSaleItem_MoreInfoInvalid_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("é树\n\t\uD83D\uDE02")
                .withQuantity(2);
        assertThrows(ValidationResponseException.class, saleItem::build);
    }

    @Test
    void createSaleItem_CloseDateInvalidFormat_ObjectNotCreated() {
        var builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withMoreInfo("What's the time, Mr Wolfy?")
                .withQuantity(2);
        assertThrows(DateTimeParseException.class, () -> builder.withCloses("In three seconds"));
    }

    @Test
    void saleItemDTO_hasAllProperties_expectPropertiesPresent() throws JsonProcessingException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withMoreInfo("This expires really soon")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        var object = objectMapper.convertValue(new SaleItemResponseDTO(saleItem), JSONObject.class);

        assertEquals(saleItem.getId(), object.get("id"));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(new InventoryItemResponseDTO(saleItem.getInventoryItem()))),
                objectMapper.readTree(objectMapper.writeValueAsString(object.get("inventoryItem")))
        );
        assertEquals(saleItem.getQuantity(), object.get("quantity"));
        assertEquals(saleItem.getPrice(), object.get("price"));
        assertEquals(saleItem.getMoreInfo(), object.get("moreInfo"));
        assertEquals(saleItem.getCreated().toString(), object.get("created"));
        assertEquals(saleItem.getCloses().toString(), object.get("closes"));
        assertEquals(saleItem.getLikeCount(), object.get("interestCount"));
        assertEquals(8, object.size()); // No extra properties
    }

    @Test
    void saleItemDTO_hasSomeProperties_expectRequiredPropertiesPresent() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        var object = objectMapper.convertValue(new SaleItemResponseDTO(saleItem), JSONObject.class);
        assertFalse(object.containsKey("moreInfo"));


        assertEquals(7, object.size());
    }

    @Test
    void findAll_saleItemExistsForBusiness_saleItemIsFound() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1, ChronoUnit.DAYS).toString())
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by("created"));

        Specification<SaleItem> specification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(testBusiness);
        Page<SaleItem> foundItems = saleItemRepository.findAll(specification, pageRequest);

        assertEquals(1, foundItems.getTotalElements());
        SaleItem foundItem = foundItems.getContent().get(0);

        assertEquals(saleItem.getId(), foundItem.getId());
        assertEquals(saleItem.getInventoryItem().getId(), foundItem.getInventoryItem().getId());
        assertEquals(saleItem.getCloses(), foundItem.getCloses());
        assertEquals(saleItem.getQuantity(), foundItem.getQuantity());
    }

    @Test
    void findAll_multipleInventoryItems_allSaleItemsAreFoundNoDuplicates() throws Exception {
        LocalDate today = LocalDate.now();

        // Creates many sale items associated with different inventory items
        Map<Long, SaleItem> saleItems = new HashMap<>();
        for (int i = 0; i<3; i++) {
            var inventoryItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(30)
                    .withPricePerItem("2.69")
                    .withManufactured("2021-03-11")
                    .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                    .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                    .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                    .build();
            inventoryItem = inventoryItemRepository.save(inventoryItem);

            for (int j = 0; j<4; j++) {
                var saleItem = new SaleItem.Builder()
                        .withInventoryItem(inventoryItem)
                        .withQuantity(1)
                        .withPrice("10.00")
                        .withMoreInfo("more_info_" + i + "_" + j)
                        .build();
                saleItem = saleItemRepository.save(saleItem);
                saleItems.put(saleItem.getId(), saleItem);
            }
        }

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by("created"));

        Specification<SaleItem> specification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(testBusiness);
        Page<SaleItem> foundItems = saleItemRepository.findAll(specification, pageRequest);

        for (SaleItem foundItem : foundItems) {
            SaleItem matchingItem = saleItems.get(foundItem.getId());
            assertNotNull(matchingItem);
            assertEquals(matchingItem.getMoreInfo(), foundItem.getMoreInfo());
        }
        assertEquals(saleItems.size(), foundItems.getTotalElements());
    }

    @Test
    void findAll_multipleProducts_allSaleItemsAreFoundNoDuplicates() throws Exception {
        LocalDate today = LocalDate.now();

        // Creates many sale items associated with different products
        Map<Long, SaleItem> saleItems = new HashMap<>();
        for (int i = 0; i<3; i++) {
            var product = new Product.Builder()
                    .withBusiness(testBusiness)
                    .withProductCode("TEST-" + i)
                    .withName("test_product")
                    .build();
            product = productRepository.save(product);
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

            for (int j = 0; j<4; j++) {
                var saleItem = new SaleItem.Builder()
                        .withInventoryItem(inventoryItem)
                        .withQuantity(1)
                        .withPrice("10.00")
                        .withMoreInfo("more_info_" + i + "_" + j)
                        .build();
                saleItem = saleItemRepository.save(saleItem);
                saleItems.put(saleItem.getId(), saleItem);
            }
        }

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(null, null, Sort.by("created"));

        Specification<SaleItem> specification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(testBusiness);
        Page<SaleItem> foundItems = saleItemRepository.findAll(specification, pageRequest);
        
        for (SaleItem foundItem : foundItems) {
            SaleItem matchingItem = saleItems.get(foundItem.getId());
            assertNotNull(matchingItem);
            assertEquals(matchingItem.getMoreInfo(), foundItem.getMoreInfo());
        }
        assertEquals(saleItems.size(), foundItems.getTotalElements());
    }

    @Test
    void addInterestedUser_addSingleUser_userAddedToInterestedUsersAndLikeCountIsOne() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);
        saleItem.addInterestedUser(testUser);
        saleItemRepository.save(saleItem);

        try (Session session = sessionFactory.openSession()) {
            saleItem = session.get(SaleItem.class, saleItem.getId());

            Set<Long> actualIds = saleItem
                    .getInterestedUsers().stream()
                    .map(User::getUserID)
                    .collect(Collectors.toSet());
            assertEquals(Set.of(testUser.getUserID()), actualIds);
            assertEquals(1, saleItem.getLikeCount());
        }
    }

    @Test
    void addInterestedUser_sameUserTwice_userAddedToInterestedUsersAndLikeCountIsOne() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        saleItem.addInterestedUser(testUser);
        saleItem.addInterestedUser(testUser);

        saleItemRepository.save(saleItem);

        try (Session session = sessionFactory.openSession()) {
            saleItem = session.get(SaleItem.class, saleItem.getId());

            Set<Long> actualIds = saleItem
                    .getInterestedUsers().stream()
                    .map(User::getUserID)
                    .collect(Collectors.toSet());
            assertEquals(Set.of(testUser.getUserID()), actualIds);
            assertEquals(1, saleItem.getLikeCount());
        }
    }

    @Test
    void addInterestedUser_addTwoUsers_userAddedToInterestedUsersAndLikeCountIsTwo() {
        User testUser2 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith96@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser2 = userRepository.save(testUser2);

        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        saleItem.addInterestedUser(testUser);
        saleItem.addInterestedUser(testUser2);

        saleItemRepository.save(saleItem);

        try (Session session = sessionFactory.openSession()) {
            saleItem = session.get(SaleItem.class, saleItem.getId());

            Set<Long> actualIds = saleItem
                    .getInterestedUsers().stream()
                    .map(User::getUserID)
                    .collect(Collectors.toSet());
            assertEquals(Set.of(testUser.getUserID(), testUser2.getUserID()), actualIds);
            assertEquals(2, saleItem.getLikeCount());
        }
    }

    @Test
    void removeInterestedUser_removeNonInterestedUser_interestedUsersUnchanged() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();

        User testUser2 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith96@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser2 = userRepository.save(testUser2);

        saleItem.addInterestedUser(testUser);
        saleItem.removeInterestedUser(testUser2);

        assertEquals(Set.of(testUser), saleItem.getInterestedUsers());
    }

    @Test
    void removeInterestedUser_removeInterestedUser_userRemoved() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        saleItem.addInterestedUser(testUser);
        saleItemRepository.save(saleItem);

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            saleItem = session.get(SaleItem.class, saleItem.getId());
            saleItem.removeInterestedUser(saleItem.getInterestedUsers().stream().findFirst().orElseThrow());
            session.save(saleItem);
            session.getTransaction().commit();
        }

        try (Session session = sessionFactory.openSession()) {
            saleItem = session.get(SaleItem.class, saleItem.getId());

            assertEquals(Set.of(), saleItem.getInterestedUsers());
            assertEquals(0, saleItem.getLikeCount());
        }
    }
}
