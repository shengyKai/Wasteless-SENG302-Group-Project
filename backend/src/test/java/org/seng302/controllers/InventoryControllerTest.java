package org.seng302.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.seng302.entities.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private InventoryController inventoryController;
    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();

    @Mock
    private HttpServletRequest request;
    @MockBean
    private BusinessRepository businessRepository;
    @MockBean
    private InventoryItemRepository inventoryItemRepository;
    @MockBean
    private ProductRepository productRepository;

    private User testUser;
    @Mock
    private Business testBusiness;
    @Mock
    private HttpSession session;
    private Product testProduct;

    @BeforeEach
    public void setup() throws ParseException {
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
    }

    @Test
    void addInventory_validPermission_canAddInventory() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isOk());
    }


    @Test
    void addInventory_normalUser_cannotAddInventory403() throws Exception {
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager
                    .when(() -> AuthenticationTokenManager
                            .checkAuthenticationToken(any()))
                    .then(invocation -> null); // mock a valid session
            Business businessSpy = spy(testBusiness);
            when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
            sessionAuthToken.put("accountId", 1L); // use a random account

            String inventory = "{\n" +
                    "  \"productId\": \"BEANS\",\n" +
                    "  \"quantity\": 4,\n" +
                    "  \"pricePerItem\": 6.5,\n" +
                    "  \"totalPrice\": 21.99,\n" +
                    "  \"manufactured\": \"2021-05-12\",\n" +
                    "  \"sellBy\": \"2021-05-12\",\n" +
                    "  \"bestBefore\": \"2021-05-12\",\n" +
                    "  \"expires\": \"2021-05-12\"\n" +
                    "}";
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/businesses/1/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inventory)
                    .sessionAttrs(sessionAuthToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void addInventory_notLoggedIn_cannotAddInventory401() throws Exception {
        Business businessSpy = spy(testBusiness);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addInventory_invalidBusinessId_406Thrown() throws Exception {
        when(businessRepository.getBusinessById(any())).thenCallRealMethod();
        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/999/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_noRequestBody_400Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication


        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addInventory_productNotExist_406Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenCallRealMethod(); // use real method
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_productOfWrongBusiness_403Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        Optional<Product> optionalProduct = Optional.of(productSpy);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)); // make this method always throw
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isForbidden());
    }


    @Test
    void addInventory_inventoryCreated_inventorySavedToDatabase() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication


        String inventory = "{\n" +
                "  \"productId\": \"BEANS\",\n" +
                "  \"quantity\": 4,\n" +
                "  \"pricePerItem\": 6.5,\n" +
                "  \"totalPrice\": 21.99,\n" +
                "  \"manufactured\": \"2021-05-12\",\n" +
                "  \"sellBy\": \"2021-05-12\",\n" +
                "  \"bestBefore\": \"2021-05-12\",\n" +
                "  \"expires\": \"2021-05-12\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventory))
                .andExpect(status().isOk());

        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));

    }

}



