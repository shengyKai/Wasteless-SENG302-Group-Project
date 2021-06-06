package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.entities.SaleItem;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.SaleItemRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class ProductControllerEditTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private Business business;
    @Mock
    private Product product;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private ProductController productController;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Setup mock business
        when(business.getId()).thenReturn(1L);

        when(businessRepository.getBusinessById(1L)).thenReturn(business);
        when(businessRepository.getBusinessById(not(eq(1L)))).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        // Setup mock product
        when(productRepository.getProduct(any(Business.class), any(String.class))).thenAnswer(CALLS_REAL_METHODS);

        when(productRepository.findByBusinessAndProductCode(business, "APPLE-1")).thenReturn(Optional.of(product));
        when(productRepository.findByBusinessAndProductCode(eq(business), not(eq("APPLE-1")))).thenReturn(Optional.empty());
        when(productRepository.findByBusinessAndProductCode(not(eq(business)), any(String.class))).thenReturn(Optional.empty());

        productController = new ProductController(productRepository, businessRepository, null, null);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    /**
     * Creates a valid request body for the /businesses/:id/products endpoint
     *
     * @return A JSONObject that is to be used in a POST /businesses/:id/products request
     */
    private JSONObject generateProductCreationInfo() {
        JSONObject productInfo = new JSONObject();
        productInfo.put("id", "WATT-420-BEANS");
        productInfo.put("name", "Watties Baked Beans - 420g can");
        productInfo.put("description", "Baked Beans as they should be.");
        productInfo.put("manufacturer", "Heinz Wattie's Limited");
        productInfo.put("recommendedRetailPrice", 2.2);
        return productInfo;
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    @Test
    void editProduct_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                    .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the POST request
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void editProduct_validAuthToken_not401Response() throws Exception {
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateProductCreationInfo().toString()))
                    .andExpect(status().is(Matchers.not(401)))
                    .andReturn();
        // Checks that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void editProduct_businessDoesNotExist_406Response() throws Exception {
        mockMvc.perform(put("/businesses/100/products/APPLE-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateProductCreationInfo().toString()))
                    .andExpect(status().isNotAcceptable())
                    .andReturn();
    }

    @Test
    void editProduct_cannotActAsBusiness_403Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(business).checkSessionPermissions(any());

        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(business).checkSessionPermissions(any());
    }

    @Test
    void editProduct_noRequestBody_400Response() throws Exception {
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void editProduct_fieldsProvided_200ResponseAndModified() throws Exception {
        var object = generateProductCreationInfo();
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Fields should be set
        verify(product).setProductCode(object.getAsString("id"));
        verify(product).setName(object.getAsString("name"));
        verify(product).setDescription(object.getAsString("description"));
        verify(product).setManufacturer(object.getAsString("manufacturer"));
        verify(product).setRecommendedRetailPrice(object.getAsString("recommendedRetailPrice"));

        // Product should be saved
        verify(productRepository).save(product);
    }

    @Test
    void editProduct_productCodeInUse_409Response() throws Exception {
        var otherProduct = mock(Product.class);

        var object = generateProductCreationInfo();
        when(productRepository.findByBusinessAndProductCode(business, object.getAsString("id"))).thenReturn(Optional.of(otherProduct));

        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    void editProduct_invalidProductCode_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(product).setProductCode(any(String.class));
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void editProduct_invalidName_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(product).setName(any(String.class));
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void editProduct_invalidDescription_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(product).setDescription(any(String.class));
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void editProduct_invalidManufacturer_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(product).setManufacturer(any(String.class));
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void editProduct_invalidRecommendedRetailPrice_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(product).setRecommendedRetailPrice(any(String.class));
        mockMvc.perform(put("/businesses/1/products/APPLE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateProductCreationInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
