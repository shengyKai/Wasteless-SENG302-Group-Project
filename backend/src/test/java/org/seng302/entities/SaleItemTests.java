package org.seng302.entities;

import org.junit.jupiter.api.*;
import org.seng302.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaleItemTests {

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

    InventoryItem invItem;

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
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser = userRepository.save(testUser);

        Business testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
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
        Product testProduct = productRepository.save(product);

        invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy("2021-05-21")
                .withBestBefore("2021-05-28")
                .withExpires("2021-06-01")
                .build();
        inventoryItemRepository.save(invItem);
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
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_OnlyCompulsaryFieldsFilled_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_ClosesSetToday_ObjectCreated() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses(today)
                .withMoreInfo("This expires really soon")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    @Test
    void createSaleItem_ClosesSetYesterday_ObjectNotCreated() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String yesterday = formatter.format(yesterday());
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses(yesterday)
                    .withMoreInfo("This has already closed")
                    .withPrice("200.34")
                    .withQuantity(2)
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("You cannot set close dates in the past", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_NoInventoryItem_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("200.34")
                    .withQuantity(2)
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Cannot sell something that is not in your inventory", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_NoInventoryPriceAndNoSalePrice_SalePriceSetToZero() throws Exception {
        invItem.setPricePerItem(null);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2)
                .build();
        Assertions.assertNull(saleItem.getPrice());
    }

    @Test
    void createSaleItem_SalePriceNull_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice(null)
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_SalePriceUnexpectedInput_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("three dollars")
                    .withQuantity(2)
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Please enter a valid number", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_QuantityNull_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("3.57")
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Please enter a number of items between 1 and your current stock not on sale", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_QuantityZero_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withQuantity(0)
                    .withPrice("3.57")
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Please enter a number of items between 1 and your current stock not on sale", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_QuantityGreaterThanInventoryTotal_ObjectNotCreated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withQuantity(2000)
                    .withPrice("3.57")
                    .build();
            saleItemRepository.save(saleItem);
            Assertions.fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Cannot sell more items than you have", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToInventoryTotal_ObjectsCreated() throws Exception {
        invItem.setQuantity(10);
        invItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        SaleItem saleItem2 = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem2);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
        Assertions.assertNotNull(saleItemRepository.findById(saleItem2.getSaleId()));
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToGreaterThanInventoryTotal_LastObjectNotCreated() throws Exception {
        invItem.setQuantity(10);
        invItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
        SaleItem.Builder saleItem2 = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(6)
                .withPrice("3.57");
        assertThrows(ResponseStatusException.class, saleItem2::build);
    }

    @Test
    void deleteInventoryItem_SaleItemDeleted() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);

        inventoryItemRepository.deleteAll();
        Optional<SaleItem> foundItem = saleItemRepository.findById(saleItem.getSaleId());
        if (foundItem.isPresent()) { Assertions.fail(); }
    }

    @Test
    void editSaleItem_QuantityStillWithinLimits_SaleItemAndInventoryItemQuantitiesUpdated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        saleItem.setQuantity(3);
        assertEquals(3, saleItem.getQuantity());
        assertEquals(0, invItem.getRemainingQuantity());
    }

    @Test
    void editSaleItem_QuantityGreaterThanInventoryAvailable_NotUpdated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(invItem)
                    .withCloses("2034-12-25")
                    .withMoreInfo("This doesn't expire for a long time")
                    .withQuantity(2)
                    .withPrice("3.57")
                    .build();
            saleItemRepository.save(saleItem);
            saleItem.setQuantity(5);
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Cannot sell more items than you have", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void autogeneratePrice_PriceSetBasedOffInventoryPricePerItem() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        assertEquals(invItem.getPricePerItem().multiply(new BigDecimal(2)), saleItem.getPrice());
    }

    @Test
    void autogeneratePrice_InventoryHasNoPriceEither_PriceNull() throws Exception {
        invItem.setPricePerItem(null);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNull(saleItem.getPrice());
    }

    @Test
    void createSaleItem_MoreInfoTooLong_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. ")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_MoreInfoInvalid_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("2034-12-25")
                .withMoreInfo("é树\n\t\uD83D\uDE02")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_CloseDateInvalidFormat_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(invItem)
                .withCloses("In three seconds")
                .withMoreInfo("What's the time, Mr Wolfy?")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }
}
