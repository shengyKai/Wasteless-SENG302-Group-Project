package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerModifyInvEntriesTest {
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
    @Mock
    private Business testBusiness;
    @Mock
    private Business mockBusiness;
    @Mock
    private List<Product> mockProductList;

    private Product testProduct;
    private Product testProduct2;
    private Product testProduct3;
    private Product testProductNull;

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
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser)
                .build();
        testProduct = new Product.Builder()
                .withProductCode("BEANS")
                .withBusiness(testBusiness)
                .withDescription("some description")
                .withManufacturer("manufacturer")
                .withName("some Name")
                .withRecommendedRetailPrice("15")
                .build();
        testProduct2 = new Product.Builder()
                .withProductCode("HAM")
                .withBusiness(testBusiness)
                .withDescription("some description 2")
                .withManufacturer("manufacturer 2")
                .withName("some Name 2")
                .withRecommendedRetailPrice("16")
                .build();
        testProduct3 = new Product.Builder()
                .withProductCode("VEGE")
                .withBusiness(testBusiness)
                .withDescription("another description")
                .withManufacturer("another manufacturer")
                .withName("another Name")
                .withRecommendedRetailPrice("17")
                .build();
        testProductNull = new Product.Builder()
                .withProductCode("ZZZ")
                .withBusiness(testBusiness)
                .withName("zzz")
                .build();

        List<InventoryItem> inventory = new ArrayList<>();
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2028-01-01")
                .withPricePerItem("1").withTotalPrice("1").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(2).withExpires("2028-01-01")
                .withPricePerItem("2").withTotalPrice("2").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(3).withExpires("2029-01-01")
                .withPricePerItem("3").withTotalPrice("3").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(4).withExpires("2029-01-01")
                .withPricePerItem("4").withTotalPrice("4").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(5).withExpires("2030-06-06")
                .withPricePerItem("5").withTotalPrice("5").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(6).withExpires("2030-06-06")
                .withPricePerItem("6").withTotalPrice("6").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProductNull).withQuantity(7).withExpires("2031-06-06")
                .build());

        Business businessSpy = spy(testBusiness);
        when(businessSpy.getId()).thenReturn(1L);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication
        when(productRepository.findAllByBusiness(any())).thenReturn(mockProductList);
        when(invItemRepository.getInventoryByCatalogue(any())).thenReturn(inventory);

        InventoryController invController = new InventoryController(businessRepository, invItemRepository,
                productRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(invController).build();

        //TODO create inventory item
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

    @Test
    void modifyInvEntries_notLoggedIn_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_invalidAuthToken_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyId_modifiedInvEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("productId", "NathanAppple95");

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyIdInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        int[] productIds = {-1, -2, 0, 1, 1000};

        for (int i=0; i <= productIds.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("quantity", productIds[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyIdNull_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("productId", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyQuantity_modifiedInvEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        //tests a wide range of integers
        for (int quantity=1; quantity <= 100; quantity++) {
            JSONObject invBody = new JSONObject();
            invBody.put("quantity", quantity);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void modifyInvEntries_modifyQuantityInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] quantities = {"-1", "-2", "-10", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= quantities.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("quantity", quantities[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyQuantityNull_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("quantity", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyPricePerItem_modifiedInvEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        //tests a wide range of floats
        for (int price=1; price <= 99; price++) {
            float pricePerItem = price + (((float)price) / 100);
            JSONObject invBody = new JSONObject();
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
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] prices = {"-1.01", "-0.01", "-2000", "-1000.99", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= prices.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("pricePerItem", prices[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyPricePerItemNull_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("pricePerItem", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyTotalPrice_modifiedInvEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        //tests a wide range of floats
        for (int price=1; price <= 99; price++) {
            float totalPrice = price + (((float)price) / 100);
            JSONObject invBody = new JSONObject();
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
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] prices = {"-21.01", "-0.01", "-9000", "-3000.99", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= prices.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("totalPrice", prices[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyTotalPriceNull_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("totalPrice", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedDate_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedDateInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] manufacturedDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= manufacturedDates.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("manufactured", manufacturedDates[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyManufacturedDateNull_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedDateAfterToday_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySelByDate_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByDateInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] sellByDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= sellByDates.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("sellBy", sellByDates[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifySellByDateNull_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByDateBeforeToday_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDate_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("bestBefore", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDateInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] bestBeforeDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= bestBeforeDates.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("bestBefore", bestBeforeDates[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDateNull_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("bestBefore", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeDateBeforeToday_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("bestBefore", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyExpiresDate_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("expires", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyExpiresDateInvalid_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        String[] expiresDates = {"10-10", "10/10/1999", "999999", "#", "$", "a", "b", "Z" };

        for (int i=0; i <= expiresDates.length; i++) {
            JSONObject invBody = new JSONObject();
            invBody.put("expires", expiresDates[i]);

            mockMvc.perform(MockMvcRequestBuilders
                    .put("/businesses/1/inventory/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invBody.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void modifyInvEntries_modifyExpiresDateNull_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("pricePerItem", null);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyExpiresDateBeforeToday_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("expires", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeSellBy_ModifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());
        invBody.put("sellBy", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterSellBy_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().plusYears(1).toString());
        invBody.put("sellBy", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeBestBefore_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterBestBefore_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().plusYears(1).toString());
        invBody.put("bestBefore", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyManufacturedBeforeExpires_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());
        invBody.put("expires", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyManufacturedAfterExpires_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("manufactured", LocalDate.now().plusYears(1).toString());
        invBody.put("expires", LocalDate.now().minusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySellByBeforeBestBefore_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().plusYears(1).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(2).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByAfterBestBefore_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().plusYears(2).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifySellByBeforeExpires_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().plusYears(1).toString());
        invBody.put("expires", LocalDate.now().plusYears(2).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifySellByAfterExpires_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("sellBy", LocalDate.now().plusYears(2).toString());
        invBody.put("expires", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeBeforeExpires_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("bestBefore", LocalDate.now().plusYears(1).toString());
        invBody.put("expires", LocalDate.now().plusYears(2).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_modifyBestBeforeAfterExpires_cannotModify400() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("bestBefore", LocalDate.now().plusYears(2).toString());
        invBody.put("expires", LocalDate.now().plusYears(1).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifyInvEntries_modifyAllFields_modifiedEntry200() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        when(productRepository.getProduct(any(), any())).thenReturn(productSpy);
        doNothing().when(businessSpy).checkSessionPermissions(any());

        JSONObject invBody = new JSONObject();
        invBody.put("productId", "NathanApple52");
        invBody.put("quantity", 10);
        invBody.put("pricePerItem", 5.42);
        invBody.put("totalPrice", 54.20);
        invBody.put("manufactured", LocalDate.now().minusYears(1).toString());
        invBody.put("sellBy", LocalDate.now().plusYears(1).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(2).toString());
        invBody.put("expires", LocalDate.now().plusYears(3).toString());

        mockMvc.perform(MockMvcRequestBuilders
                .put("/businesses/1/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void modifyInvEntries_isDGAA_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsBusinessOwner_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsDifferentBusinessOwner_cannotModify403() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsBusinessAdmin_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsDifferentBusinessAdmin_cannotModify403() {
        //TODO
    }

    @Test
    void modifyInvEntries_validBusinessIdAndInvItemId_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_nonexistentBusinessId_invalidId400() {
        //TODO
    }

    @Test
    void modifyInvEntries_nonexistentInvItemId_invalidId40() {
        //TODO
    }
}
