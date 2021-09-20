package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerModifyInvEntriesTest {
    @Autowired
    private MockMvc mockMvc;

    private InventoryController invController;
    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

    @MockBean
    private BusinessRepository businessRepository;
    @MockBean
    private InventoryItemRepository invItemRepository;
    @MockBean
    private ProductRepository productRepository;

    private User testUser;
    private User testUser2;
    @Mock
    private Business testBusiness;
    @Mock
    private Business testBusiness2;

    private Product testProduct;
    private Product testProduct2;
    private Product testProductNull;

    private InventoryItem testInvItem;
    private InventoryItem testInvItem2;
    private InventoryItem testInvItem3;
    private InventoryItem testInvItemNull;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        testUser = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Elliot")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testUser2 = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Elliot")
                .withNickName("Ando")
                .withEmail("1233andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1986-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testBusiness = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser)
                .build();
        testBusiness2 = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withDescription("DESCRIPTIONN")
                .withName("BUSINESS_NAMEE")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser2)
                .build();
        testProduct = new Product.Builder()
                .withProductCode("NATHANAPPLE52")
                .withBusiness(testBusiness)
                .withDescription("some description")
                .withManufacturer("manufacturer")
                .withName("some Name")
                .withRecommendedRetailPrice("15")
                .build();
        testProduct2 = new Product.Builder()
                .withProductCode("NATHANAPPLE95")
                .withBusiness(testBusiness)
                .withDescription("some description two")
                .withManufacturer("manufacturer")
                .withName("some Name two")
                .withRecommendedRetailPrice("16")
                .build();
        testProductNull = new Product.Builder()
                .withProductCode("ZZZ")
                .withBusiness(testBusiness)
                .withName("zzz")
                .build();

        testInvItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(1)
                .withExpires("2028-01-01")
                .withPricePerItem("1")
                .withTotalPrice("1")
                .withManufactured("2020-01-01")
                .withSellBy("2026-02-01")
                .withBestBefore("2027-03-01")
                .build();
        testInvItem2 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires("2028-01-01")
                .withPricePerItem("2")
                .withTotalPrice("2")
                .withManufactured("2020-01-01")
                .withSellBy("2026-02-01")
                .withBestBefore("2027-03-01")
                .build();
        testInvItemNull = new InventoryItem.Builder()
                .withProduct(testProductNull)
                .withQuantity(7)
                .withExpires("2031-06-06")
                .build();

        testBusiness.addToCatalogue(testProduct);
        testBusiness.addToCatalogue(testProduct2);

        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        Product productSpy2 = spy(testProduct2);
        InventoryItem invItemSpy = spy(testInvItem);
        when(businessRepository.getBusinessById(1L)).thenReturn(businessSpy);
        when(businessRepository.getBusinessById(not(eq(1L)))).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        when(productRepository.getProduct(businessSpy, "NATHANAPPLE52")).thenReturn(productSpy);
        when(productRepository.getProduct(businessSpy, "NATHANAPPLE95")).thenReturn(productSpy2);
        when(productRepository.findByBusinessAndProductCode(businessSpy, "NATHANAPPLE52"))
                .thenReturn(java.util.Optional.ofNullable(productSpy));
        when(productRepository.findByBusinessAndProductCode(businessSpy, "NATHANAPPLE95"))
                .thenReturn(java.util.Optional.ofNullable(productSpy2));

        when(invItemRepository.findInventoryItemByBusinessAndId(businessSpy, 1L)).thenReturn(Optional.of(invItemSpy));
        when(invItemRepository.findInventoryItemByBusinessAndId(eq(businessSpy), not(eq(1L)))).thenReturn(Optional.empty());

        doNothing().when(businessSpy).checkSessionPermissions(any());
    }

    /**
     * Removes everything from the repositories to get create a fresh environment for the next test.
     */
    @AfterEach
    public void cleanUp() {
        invItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
    }

    /**
     * Generates a mock JSON inventory item JSON body to be used in the modify inventory entries API endpoint
     * @return inventory item JSON body
     */
    public JSONObject generateInvJSONBody() {
        JSONObject invBody = new JSONObject();
        invBody.put("productId", "NATHANAPPLE52");
        invBody.put("quantity", 10);
        invBody.put("pricePerItem", 5.42);
        invBody.put("totalPrice", 54.20);
        invBody.put("manufactured", LocalDate.now().minusYears(100).toString());
        invBody.put("sellBy", LocalDate.now().plusYears(100).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(200).toString());
        invBody.put("expires", LocalDate.now().plusYears(300).toString());
        return invBody;
    }

    @Test
    void modifyInvEntries_modifyId_modifiedInvEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("productId", "NATHANAPPLE95");

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_productNotFound_cannotModify406() throws Exception {
        int[] productIds = {-1, 999999, 0};
        JSONObject invBody = generateInvJSONBody();

        for (int productId : productIds) {
            invBody.put("productId", productId);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isNotAcceptable());
        }
    }

    @Test
    void modifyInvEntries_modifyQuantity_modifiedInvEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();

        //tests a wide range of integers
        for (int quantity=1; quantity < 100; quantity++) {
            invBody.put("quantity", quantity);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isOk());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-2", "-10", "#", "$", "a", "b", "Z" })
    void modifyInvEntries_modifyQuantityInvalid_cannotModify400(String quantity) throws Exception {
        JSONObject invBody = generateInvJSONBody();

        invBody.put("quantity", quantity);
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyPricePerItem_modifiedInvEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();

        //tests a wide range of floats
        for (int price=1; price < 99; price++) {
            float pricePerItem = price + (((float)price) / 100);
            invBody.put("pricePerItem", pricePerItem);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void modifyInvEntries_modifyPricePerItemInvalid_cannotModify400() throws Exception {
        String[] prices = {"-1.01", "-0.01", "-2000", "-1000.99", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String price : prices) {
            invBody.put("pricePerItem", price);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"pricePerItem", "totalPrice", "manufactured", "sellBy", "bestBefore"})
    void modifyInvEntries_modifyOptionalFieldNull_modifiedEntry200(String field) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove(field);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"productId", "quantity", "expires"})
    void modifyInvEntries_requiredFieldNull_cannotModify400(String field) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove(field);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyTotalPrice_modifiedInvEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();

        //tests a wide range of floats
        for (int price=1; price < 99; price++) {
            float totalPrice = price + (((float)price) / 100);
            invBody.put("totalPrice", totalPrice);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void modifyInvEntries_modifyTotalPriceInvalid_cannotModify400() throws Exception {
        String[] prices = {"-21.01", "-0.01", "-9000", "-3000.99", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String price : prices) {
            invBody.put("totalPrice", price);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyManufacturedDate_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedDateInvalid_cannotModify400() throws Exception {
        String[] manufacturedDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String manufacturedDate : manufacturedDates) {
            invBody.put("manufactured", manufacturedDate);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyManufacturedDateAfterToday_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySelByDate_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByDateInvalid_cannotModify400() throws Exception {
        String[] sellByDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String sellByDate : sellByDates) {
            invBody.put("sellBy", sellByDate);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifySellByDateBeforeToday_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().minusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDate_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("bestBefore", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDateInvalid_cannotModify400() throws Exception {
        String[] bestBeforeDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String bestBeforeDate : bestBeforeDates) {
            invBody.put("bestBefore", bestBeforeDate);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDateBeforeToday_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("bestBefore", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyExpiresDate_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("expires", LocalDate.now().plusYears(400).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyExpiresDateInvalid_cannotModify400() throws Exception {
        String[] expiresDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };
        JSONObject invBody = generateInvJSONBody();

        for (String expiresDate : expiresDates) {
            invBody.put("expires", expiresDate);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyExpiresDateBeforeToday_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("expires", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeSellBy_ModifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());
        invBody.put("sellBy", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterSellBy_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().plusYears(100).toString());
        invBody.put("sellBy", LocalDate.now().minusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeBestBefore_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().minusYears(100).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterBestBefore_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().plusYears(100).toString());
        invBody.put("bestBefore", LocalDate.now().minusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeExpires_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().minusYears(900).toString());
        invBody.put("expires", LocalDate.now().plusYears(400).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterExpires_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("manufactured", LocalDate.now().plusYears(100).toString());
        invBody.put("expires", LocalDate.now().minusYears(300).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySellByBeforeBestBefore_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().plusYears(100).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(200).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByAfterBestBefore_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().plusYears(200).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySellByBeforeExpires_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().plusYears(200).toString());
        invBody.put("expires", LocalDate.now().plusYears(300).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByAfterExpires_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("sellBy", LocalDate.now().plusYears(200).toString());
        invBody.put("expires", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeBeforeExpires_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("bestBefore", LocalDate.now().plusYears(100).toString());
        invBody.put("expires", LocalDate.now().plusYears(200).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeAfterExpires_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.put("bestBefore", LocalDate.now().plusYears(200).toString());
        invBody.put("expires", LocalDate.now().plusYears(100).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyAllFields_modifiedEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_invEntryIsBusinessOwner_modifiedInvEntry200() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_invEntryIsDifferentBusinessOwner_cannotModify400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/2/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void modifyInvEntries_nonexistentBusinessId_invalidId400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/10000/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void modifyInvEntries_nonexistentInvItemId_invalidId400() throws Exception {
        JSONObject invBody = generateInvJSONBody();
        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/10000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }
}
