package org.seng302.leftovers.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.dto.product.ProductResponseDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryItemTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryItemRepository inventoryItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Business testBusiness;
    private Product testProduct;
    private InventoryItem testInvItem;

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

        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(LocalDate.now().plusWeeks(1))
                .withBestBefore(LocalDate.now().plusWeeks(2))
                .withExpires(LocalDate.now().plusWeeks(3))
                .build();
        testInvItem = inventoryItemRepository.save(invItem);
    }
    void clearDatabase() {
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
    void createInventoryItem_withAllFields_objectCreated() throws Exception {
        LocalDate today = LocalDate.now();

        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS))
                .withBestBefore(today.plus(3, ChronoUnit.DAYS))
                .withExpires(today.plus(4, ChronoUnit.DAYS))
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withOnlyRequiredFields_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Test that check mandatory cant be null
    */

    @Test
    void createInventoryItem_withNullProduct_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(null)
                    .withQuantity(2)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                    .build();
            // fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No product was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withNullExpires_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(1)
                    .withExpires((LocalDate) null)
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No expiry date was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withZeroQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(0)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withNegativeQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(-69)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            //assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    /**
     * The following test section will test attributes related to price which is 
     * Price per item, Total price
     * Test condition will be (Null, validPriceFormat, InvalidPriceFormat, 
     * LowerThanPriceRange, HigherThanPriceRange, ExactlyOnPriceRangeEnd)
     */

    @Test
    void createInventoryItem_withNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withPricePerItem((BigDecimal) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withNullTotalPriceAndNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withPricePerItem((BigDecimal) null)
                .withTotalPrice((BigDecimal) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withNullTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withTotalPrice((BigDecimal) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withPricePerItem("2.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withTotalPrice("21.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withInvalidPricePerItemFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS));
        assertThrows(ResponseStatusException.class, () -> {
            builder.withPricePerItem("xx0.1");
        });
    }
    @Test
    void createInventoryItem_withInvalidTotalPriceFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS));
        assertThrows(ResponseStatusException.class, () -> {
            builder.withTotalPrice("xx0.1");
        });
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1", "10001", "10000"})
    void createInventoryItem_withNegativePricePerItem_objectNotCreated(BigDecimal value) throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
            .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setPricePerItem(value);
        });
    }
    @ParameterizedTest
    @ValueSource(strings = {"-2", "1000001"})
    void createInventoryItem_withNegativeTotalPrice_objectNotCreated(BigDecimal value) throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setTotalPrice(value);
        });
    }
    @Test
    void createInventoryItem_withNullManufacturedDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withManufactured((LocalDate) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForManufactured_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withManufactured(acceptDate)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
        .build();
        assertNotNull(invItem);
    }
    @Test
    void createInventoryItem_withDateInFutureForManufactured_objectNotCreated() {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withManufactured("2022-03-03");
        assertThrows(ResponseStatusException.class, builder::build);
    }
    @Test
    void createInventoryItem_withInvalidManufacturedFormat_objectNotCreated() {
        InventoryItem.Builder builder = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS));

        assertThrows(DateTimeParseException.class, () -> {
            builder.withManufactured("201x-09-09");
        });
    }
    @Test
    void createInventoryItem_withNullSellByDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withSellBy((LocalDate) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForSellBy_objectNotCreated() {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withSellBy("2000-03-03");
        assertThrows(ResponseStatusException.class, builder::build);
    }
    @Test
    void createInventoryItem_withDateInFutureForSellBy_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withSellBy(acceptDate)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        assertNotNull(builder);
    }
    @Test
    void createInventoryItem_withInvalidSellByFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
            .withProduct(testProduct)
            .withQuantity(2)
            .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS));

        assertThrows(DateTimeParseException.class, () -> {
            builder.withSellBy("20x-01-01");
        });
    }
    @Test
    void createInventoryItem_withNullBestBefore_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withBestBefore((LocalDate) null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).orElseThrow();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForBestBefore_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .withBestBefore("2000-03-03");
        assertThrows(ResponseStatusException.class, builder::build);
    }
    @Test
    void createInventoryItem_withDateInFutureForBestBefore_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withBestBefore(acceptDate)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        
        assertNotNull(invItem);
    }
    @Test
    void createInventoryItem_withInvalidBestBeforeFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS));

        assertThrows(DateTimeParseException.class, () -> {
        invItem 
        .withBestBefore("2020-x-01");
        });
    }
    @Test
    void createInventoryItem_multipleInventoryItemsHaveSameProduct_objectCreated() throws Exception {
        InventoryItem invItem1 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        InventoryItem invItem2 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(100, ChronoUnit.DAYS))
                .build();
        InventoryItem invItem3 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(4)
                .withExpires(LocalDate.now().plus(150, ChronoUnit.DAYS))
                .build();
        inventoryItemRepository.save(invItem1);
        inventoryItemRepository.save(invItem2);
        inventoryItemRepository.save(invItem3);
        InventoryItem testInvItem1 = inventoryItemRepository.findById(invItem1.getId()).orElseThrow();
        InventoryItem testInvItem2 = inventoryItemRepository.findById(invItem2.getId()).orElseThrow();
        InventoryItem testInvItem3 = inventoryItemRepository.findById(invItem3.getId()).orElseThrow();
        assertEquals(invItem1, testInvItem1);
        assertEquals(invItem2, testInvItem2);
        assertEquals(invItem3, testInvItem3);
    }

    @Test
    void inventoryItemDTO_noNullAttributes_returnsExpectedJson() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withTotalPrice("5.32")
                .withManufactured("2021-03-11")
                .withSellBy(LocalDate.now().plus(10, ChronoUnit.DAYS))
                .withBestBefore(LocalDate.now().plus(20, ChronoUnit.DAYS))
                .withExpires(LocalDate.now().plus(30, ChronoUnit.DAYS))
                .build();
        invItem = inventoryItemRepository.save(invItem);
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("id", invItem.getId());
        expectedJson.put("product", new ProductResponseDTO(invItem.getProduct()));
        expectedJson.put("quantity", invItem.getQuantity());
        expectedJson.put("remainingQuantity", invItem.getRemainingQuantity());
        expectedJson.put("pricePerItem", invItem.getPricePerItem());
        expectedJson.put("totalPrice", invItem.getTotalPrice());
        expectedJson.put("manufactured", invItem.getManufactured());
        expectedJson.put("sellBy", invItem.getSellBy());
        expectedJson.put("bestBefore", invItem.getBestBefore());
        expectedJson.put("expires", invItem.getExpires());
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(expectedJson)),
                objectMapper.readTree(objectMapper.writeValueAsString(new InventoryItemResponseDTO(invItem)))
        );
    }

    @Test
    void constructJSONObject_optionalAttributesNull_returnsExpectedJson() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        invItem = inventoryItemRepository.save(invItem);
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("id", invItem.getId());
        expectedJson.put("product", new ProductResponseDTO(invItem.getProduct()));
        expectedJson.put("quantity", invItem.getQuantity());
        expectedJson.put("remainingQuantity", invItem.getRemainingQuantity());
        expectedJson.put("expires", invItem.getExpires());
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(objectMapper.writeValueAsString(expectedJson)),
                mapper.readTree(objectMapper.writeValueAsString(new InventoryItemResponseDTO(invItem))));
    }

    @Test
    void saveInventoryItem_saveMultipleWithSameVersion_throwsException() throws Exception {
        InventoryItem originalItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        originalItem = inventoryItemRepository.save(originalItem);


        InventoryItem copy1 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        InventoryItem copy2 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();

        copy1.setQuantity(4);
        copy2.setQuantity(5);

        assertDoesNotThrow(() -> inventoryItemRepository.save(copy1));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> inventoryItemRepository.save(copy2));
    }

    @Test
    void saveInventoryItem_saveMultipleWithDifferentVersion_savesCorrectly() throws Exception {
        InventoryItem originalItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS))
                .build();
        originalItem = inventoryItemRepository.save(originalItem);


        InventoryItem copy1 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        copy1.setQuantity(4);
        assertDoesNotThrow(() -> inventoryItemRepository.save(copy1));

        InventoryItem copy2 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        copy2.setQuantity(5);
        assertDoesNotThrow( () -> inventoryItemRepository.save(copy2));
    }

    //--- Date validation modify ---//

    /**
     * Creates a valid manufactured, sell by, best before and expires date.
     * @return a list containing the four generated valid dates.
     */
    public List<LocalDate> generateValidDates() {
        var today = LocalDate.now();
        return List.of(
                today.minusYears(1), // Manufactured
                today.plusYears(1),  // Sell by
                today.plusYears(2),  // Best before
                today.plusYears(3)   // Expires
        );
    }

    // Basic date setting
    @Test
    void modifyInventoryItem_changeAllDatesToValidDate_allDatesChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        invItem.setDates(dates.get(0), dates.get(1), dates.get(2), dates.get(3));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(invItemRepo.getManufactured(), dates.get(0));
        assertEquals(invItemRepo.getSellBy(), dates.get(1));
        assertEquals(invItemRepo.getBestBefore(), dates.get(2));
        assertEquals(invItemRepo.getExpires(), dates.get(3));
    }

    // Null testing
    @Test
    void modifyInventoryItem_changeManufacturedToNull_manufacturedDateSetToNull() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        invItem.setDates(null, dates.get(1), dates.get(2), dates.get(3));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertNull(invItemRepo.getManufactured());
        assertEquals(dates.get(1), invItemRepo.getSellBy());
        assertEquals(dates.get(2), invItemRepo.getBestBefore());
        assertEquals(dates.get(3), invItemRepo.getExpires());
    }

    @Test
    void modifyInventoryItem_changeSellByToNull_sellByDateSetToNull() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        invItem.setDates(dates.get(0), null, dates.get(2), dates.get(3));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(dates.get(0), invItemRepo.getManufactured());
        assertNull(invItemRepo.getSellBy());
        assertEquals(dates.get(2), invItemRepo.getBestBefore());
        assertEquals(dates.get(3), invItemRepo.getExpires());
    }

    @Test
    void modifyInventoryItem_changeBestBeforeToNull_bestBeforeDateSetToNull() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        invItem.setDates(dates.get(0), dates.get(1), null, dates.get(3));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(dates.get(0), invItemRepo.getManufactured());
        assertEquals(dates.get(1), invItemRepo.getSellBy());
        assertNull(invItemRepo.getBestBefore());
        assertEquals(dates.get(3), invItemRepo.getExpires());
    }

    @Test
    void modifyInventoryItem_changeExpiresToNull_ExceptionThrownAndExpiresDateNotSetToNull() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalManufactured = invItem.getManufactured();
        LocalDate originalSellBy = invItem.getSellBy();
        LocalDate originalBestBefore = invItem.getBestBefore();
        LocalDate originalExpires = invItem.getExpires();

        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        var bestBefore = dates.get(2);

        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellBy, bestBefore, null));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(originalManufactured, invItemRepo.getManufactured());
        assertEquals(originalSellBy, invItemRepo.getSellBy());
        assertEquals(originalBestBefore, invItemRepo.getBestBefore());
        assertEquals(originalExpires, invItemRepo.getExpires());
    }

    @Test
    void modifyInventoryItem_changeManufacturedSellByAndBestBeforeDatesToNull_respectiveDatesSetToNull() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        invItem.setDates(null, null, null, dates.get(3));
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertNull(invItemRepo.getManufactured());
        assertNull(invItemRepo.getSellBy());
        assertNull(invItemRepo.getBestBefore());
        assertEquals(dates.get(3), invItemRepo.getExpires());
    }

    @Test
    void modifyInventoryItem_changeAllDatesToNull_noDatesSetToNull() throws Exception {
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalManufactured = invItem.getManufactured();
        LocalDate originalSellBy = invItem.getSellBy();
        LocalDate originalBestBefore = invItem.getBestBefore();
        LocalDate originalExpires = invItem.getExpires();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setDates(null, null, null, null);
        });
        inventoryItemRepository.save(invItem);
        InventoryItem invItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(invItemRepo.getManufactured(), originalManufactured);
        assertEquals(invItemRepo.getSellBy(), originalSellBy);
        assertEquals(invItemRepo.getBestBefore(), originalBestBefore);
        assertEquals(invItemRepo.getExpires(), originalExpires);
    }

    // Checking against today's date
    @Test
    void modifyInventoryItem_changeManufacturedToDayAfterToday_exceptionThrownAndManufacturedDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalManufactured = invItem.getManufactured();
        var manufacturedAfterToday = LocalDate.now().plusDays(1);
        var sellBy = dates.get(1);
        var bestBefore = dates.get(2);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufacturedAfterToday, sellBy, bestBefore, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getManufactured(), originalManufactured);
    }

    @Test
    void modifyInventoryItem_changeManufacturedToTenYearsAfterToday_exceptionThrownAndManufacturedDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalManufactured = invItem.getManufactured();
        var manufacturedAfterToday = LocalDate.now().plusYears(10);
        var sellBy = dates.get(1);
        var bestBefore = dates.get(2);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufacturedAfterToday, sellBy, bestBefore, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getManufactured(), originalManufactured);
    }

    @Test
    void modifyInventoryItem_changeSellByToDayBeforeToday_exceptionThrownAndSellByDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalSellBy = invItem.getSellBy();
        var sellByBeforeToday = LocalDate.now().minusDays(1);
        var manufactured = dates.get(0);
        var bestBefore = dates.get(2);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellByBeforeToday, bestBefore, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getSellBy(), originalSellBy);
    }

    @Test
    void modifyInventoryItem_changeSellByToTenYearsBeforeToday_exceptionThrownAndSellByDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalSellBy = invItem.getSellBy();
        var sellByBeforeToday = LocalDate.now().minusYears(10);
        var manufactured = dates.get(0);
        var bestBefore = dates.get(2);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellByBeforeToday, bestBefore, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getSellBy(), originalSellBy);
    }

    @Test
    void modifyInventoryItem_changeBestBeforeToDayBeforeToday_exceptionThrownAndBestBeforeDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalBestBefore = invItem.getBestBefore();
        var bestBeforeBeforeToday = LocalDate.now().minusDays(1);
        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellBy, bestBeforeBeforeToday, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getBestBefore(), originalBestBefore);
    }

    @Test
    void modifyInventoryItem_changeBestBeforeToTenYearsBeforeToday_exceptionThrownAndBestBeforeDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalBestBefore = invItem.getBestBefore();
        var bestBeforeBeforeToday = LocalDate.now().minusYears(10);
        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellBy, bestBeforeBeforeToday, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getBestBefore(), originalBestBefore);
    }

    @Test
    void modifyInventoryItem_changeExpiresToDayBeforeToday_exceptionThrownAndExpiresDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalExpires = invItem.getExpires();
        var expiresBeforeToday = LocalDate.now().minusDays(1);
        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        var bestBefore = dates.get(2);
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setDates(manufactured, sellBy, bestBefore, expiresBeforeToday);
        });
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getExpires(), originalExpires);
    }

    @Test
    void modifyInventoryItem_changeExpiresToTenYearsBeforeToday_exceptionThrownAndExpiresDateNotChanged() throws Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalExpires = invItem.getExpires();
        var expiresBeforeToday = LocalDate.now().minusYears(10);
        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        var bestBefore = dates.get(2);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, sellBy, bestBefore, expiresBeforeToday));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getExpires(), originalExpires);
    }

    // Comparing dates against each other
    @Test
    void modifyInventoryItem_changeSellByToBeAfterBestBefore_exceptionThrownAndNoDatesChanged() throws  Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalSellBy = invItem.getSellBy();
        LocalDate originalBestBefore = invItem.getBestBefore();
        var newSellBy = LocalDate.now().plusDays(12);
        var newBestBefore = LocalDate.now().plusDays(11);
        var manufactured = dates.get(0);
        var expires = dates.get(3);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, newSellBy, newBestBefore, expires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getSellBy(), originalSellBy);
        assertEquals(inventoryItemRepo.getBestBefore(), originalBestBefore);
    }

    @Test
    void modifyInventoryItem_changeBestBeforeToBeAfterExpires_exceptionThrownAndNoDatesChanged() throws  Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalBestBefore = invItem.getBestBefore();
        LocalDate originalExpires = invItem.getExpires();
        var newBestBefore = LocalDate.now().plusDays(12);
        var newExpires = LocalDate.now().plusDays(11);
        var manufactured = dates.get(0);
        var sellBy = dates.get(1);
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setDates(manufactured, sellBy, newBestBefore, newExpires);
        });
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getBestBefore(), originalBestBefore);
        assertEquals(inventoryItemRepo.getExpires(), originalExpires);
    }

    @Test
    void modifyInventoryItem_changeSellByToBeAfterExpires_exceptionThrownAndNoDatesChanged() throws  Exception {
        var dates = generateValidDates();
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, testInvItem.getId());
        LocalDate originalSellBy = invItem.getSellBy();
        LocalDate originalExpires = invItem.getExpires();
        var newSellBy = LocalDate.now().plusDays(12);
        var newExpires = LocalDate.now().plusDays(11);
        var manufactured = dates.get(0);
        var bestBefore = dates.get(2);
        assertThrows(ResponseStatusException.class, () -> invItem.setDates(manufactured, newSellBy, bestBefore, newExpires));
        InventoryItem inventoryItemRepo = inventoryItemRepository.getInventoryItemByBusinessAndId(testBusiness, invItem.getId());
        assertEquals(inventoryItemRepo.getSellBy(), originalSellBy);
        assertEquals(inventoryItemRepo.getExpires(), originalExpires);
    }
}
