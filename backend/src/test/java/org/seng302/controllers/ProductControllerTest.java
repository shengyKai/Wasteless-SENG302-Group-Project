package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.seng302.entities.Product;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private Business testBusiness1;
    private User ownerUser;
    private User bystanderUser;
    private User administratorUser;

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode() {
        String authCode = "0".repeat(64);
        sessionAuthToken.clear();
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }

    /**
     * Tags a session as DGAA
     */
    private void setUpDGAAAuthCode() {
        sessionAuthToken.put("role", "defaultGlobalApplicationAdmin");
    }
    private void setUpSessionAsAdmin() {
        sessionAuthToken.put("role", "globalApplicationAdmin");
    }

    /**
     * Mocks a session logged in as a given user
     * @param userId Log in with userId
     */
    private void setCurrentUser(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    /**
     * Created a business object for testing
     */
    public void createTestBusiness() {
        businessRepository.deleteAll();
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(ownerUser)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);
    }

    @BeforeEach
    public void setUp() throws ParseException {
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();

        setUpAuthCode();

        ownerUser = new User.Builder()
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

        ownerUser = userRepository.save(ownerUser);
        createTestBusiness();

        bystanderUser = new User.Builder()
                .withFirstName("Happy")
                .withMiddleName("Boi")
                .withLastName("Jones")
                .withNickName("HappyBoi")
                .withEmail("happyboi@gmail.com")
                .withPassword("1337-H548*nt3r2")
                .withBio("Likes long walks on the beach sometimes")
                .withDob("2008-03-11")
                .withPhoneNumber("+64 5 565 0129")
                .withAddress(Location.covertAddressStringToLocation("5,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        bystanderUser = userRepository.save(bystanderUser);


        administratorUser = new User.Builder()
                .withFirstName("Connor")
                .withMiddleName("Boi")
                .withLastName("Hitchcock")
                .withNickName("Hitchy")
                .withEmail("connor.hitchcock@gmail.com")
                .withPassword("hunter2")
                .withBio("Likes long walks on the beach always")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 5 565 0125")
                .withAddress(Location.covertAddressStringToLocation("5,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        administratorUser = userRepository.save(administratorUser);

        testBusiness1.addAdmin(administratorUser);
        testBusiness1 = businessRepository.save(testBusiness1);

        // Ensures that we have a copy of administratorUser with all the administered businesses
        administratorUser = userRepository.findById(administratorUser.getUserID()).get();
    }

    /**
     * Adds several products to a catalogue
     */
    public void addSeveralProductsToACatalogue() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple1")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("ALMOND-MILK-100")
                .withName("Almond Milk")
                .withDescription("Like water except bad for the environment")
                .withManufacturer("Apple2")
                .withRecommendedRetailPrice("10.02")
                .withBusiness(testBusiness1)
                .build();
        Product product3 = new Product.Builder()
                .withProductCode("COFFEE-7")
                .withName("Generic Brand Coffee")
                .withDescription("This coffee tastes exactly as you expect it would")
                .withManufacturer("Apple3")
                .withRecommendedRetailPrice("4.02")
                .withBusiness(testBusiness1)
                .build();
        Product product4 = new Product.Builder()
                .withProductCode("DARK-CHOCOLATE")
                .withName("Dark Chocolate")
                .withDescription("Would like a high cocoa concentration")
                .withManufacturer("Apple4")
                .withRecommendedRetailPrice("6.07")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        productRepository.save(product4);
    }

    /**
     * Checks that the API returns the expect products in the business's catalogue within the response body.
     */
    @Test
    void retrieveCatalogueWithSeveralProducts() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        for (Object productObject: responseBody) {
            JSONObject productJSON = (JSONObject) productObject;

            String productCode = productJSON.getAsString("id");
            Product storedProduct = productRepository.findByBusinessAndProductCode(testBusiness1, productCode);

            Instant actualCreatedDate = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(productJSON.getAsString("created")));

            assertEquals(storedProduct.getProductCode(), productCode);
            assertEquals(storedProduct.getCreated().toInstant(), actualCreatedDate);
            assertEquals(storedProduct.getRecommendedRetailPrice().toString(), productJSON.getAsString("recommendedRetailPrice"));
            assertEquals(storedProduct.getName(), productJSON.getAsString("name"));
            assertEquals(storedProduct.getDescription(), productJSON.getAsString("description"));
            assertEquals(storedProduct.getManufacturer(), productJSON.getAsString("manufacturer"));
        }
    }

    /**
     * Tests that a business with an empty catalogue is still received
     */
    @Test
    void retrieveCatalogueWithZeroProducts() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertTrue(responseBody.isEmpty());
    }

    /**
     * Tests that when an invalid auth token is provided an unauthorised response code
     */
    @Test
    void invalidAuthTokenThenCannotViewCatalogue() throws Exception {
        mockMvc.perform(
                get(String.format("/businesses/%d/products", testBusiness1.getId())))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    /**
     * Tests that when a business with the given ID does not exist the API returns a not acceptable response code.
     */
    @Test
    void businessDoesNotExistThenNotAccepted() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        businessRepository.deleteAll();
        mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    /**
     *  Tests that a user that is not a GAA, DGAA, business owner or business admin cannot see the business catalogue.
     */
    @Test
    void userIsNotAnAdminThenForbidden() throws Exception {
        setCurrentUser(bystanderUser.getUserID());
        mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    /**
     * Tests that a business administrator can see see the business catalogue
     */
    @Test
    void userIsABusinessAdminCanRetrieveCatalogue() throws Exception {
        setCurrentUser(administratorUser.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertTrue(responseBody.isEmpty());
    }

    /**
     * Tests that the DGAA can see a businesses catalogue, even if they are not associated with the business.
     */
    @Test
    void userIsDGAACanRetrieveCatalogue() throws Exception {
        setCurrentUser(bystanderUser.getUserID());
        setUpDGAAAuthCode();
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertTrue(responseBody.isEmpty());
    }

    /**
     * Tests that a GAA can see a businesses catalogue, even if they are not associated with the business.
     */
    @Test
    void userIsGAACanRetrieveCatalogue() throws Exception {
        setCurrentUser(bystanderUser.getUserID());
        setUpSessionAsAdmin();
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertTrue(responseBody.isEmpty());
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

    /**
     * Tests that using the POST /bussinesses/:id/products without a request body results in a 400 response
     */
    @Test
    void postingAProductWithoutARequestBody() {
        setCurrentUser(ownerUser.getUserID());
        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isBadRequest())
                .andReturn());
    }

    /**
     * Tests that using the POST /businesses/:id/products endpoint adds a product that is returned from
     * Product.Builder.build to the businesses catalogue.
     */
    @Test
    void postingAProductAddsItToTheCatalogue() {
        setCurrentUser(ownerUser.getUserID());
        // The mock result
        Product mockedResult = new Product.Builder()
                .withProductCode("NATHAN-APPLE-71")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.05")
                .withBusiness(testBusiness1)
                .build();

        try (MockedConstruction<Product.Builder> mocked = Mockito.mockConstruction(Product.Builder.class, withSettings().defaultAnswer(RETURNS_SELF), (mock, context) -> {
            when(mock.build()).thenReturn(mockedResult); // Makes sure that build returns a valid product
        })) {
            setCurrentUser(ownerUser.getUserID());
            // The value inside here will be ignored
            var productInfo = generateProductCreationInfo();

            assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                    .content(productInfo.toString())
                    .sessionAttrs(sessionAuthToken)
                    .contentType("application/json")
                    .cookie(authCookie))
                    .andExpect(status().isCreated())
                    .andReturn());

            // Checks that exactly 1 product builder was instantiated
            assertEquals(1, mocked.constructed().size());
            var mockBuilder = mocked.constructed().get(0);

            // Checks that the product was built
            verify(mockBuilder).build();

            Product addedProduct = productRepository.findByBusinessAndProductCode(testBusiness1, mockedResult.getProductCode());

            // Check that the added product is equivalent to the result from Product.Builder.build.
            assertNotNull(addedProduct);
            assertEquals(mockedResult.getName(), addedProduct.getName());
            assertEquals(mockedResult.getManufacturer(), addedProduct.getManufacturer());
            assertEquals(mockedResult.getDescription(), addedProduct.getDescription());
            assertEquals(mockedResult.getRecommendedRetailPrice(), addedProduct.getRecommendedRetailPrice());
        }
    }

    /**
     * Tests that trying the add a product via the POST /businesses/:id/products endpoint results in a 406 if there is
     * no business with that id.
     */
    @Test
    void postingProductToNonExistentBusiness() {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post("/businesses/999999999/products")
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable())
                .andReturn());
    }

    /**
     * Tests that posting two products with the same product code results in a 400 response on the second product post
     */
    @Test
    void postingTwoProductsWithSameProductCodeToSameBusiness() {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isConflict())
                .andReturn());
    }

    /**
     * Tests that posting two products with the same product code results in a 201 response, if the requests are made on
     * different businesses
     */
    @Test
    void postingTwoProductsWithSameProductCodeToDifferentBusinesses() {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        Business tempBusiness = businessRepository.save(
                new Business.Builder()
                    .withBusinessType("Accommodation and Food Services")
                    .withAddress(new Location())
                    .withDescription("Some description2")
                    .withName("BusinessName2")
                    .withPrimaryOwner(ownerUser)
                    .build()
        );

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", tempBusiness.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /**
     * Tests that posting a product creates a Product.Builder and puts all the fields into said product builder.
     */
    @Test
    void postingProductPassesTheValuesToTheProductBuilder() {
        // This just has to be some value that will not crash when productRepository.save is called on it.
        Product mockedResult = new Product.Builder()
                .withProductCode("NATHAN-APPLE-71")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.05")
                .withBusiness(testBusiness1)
                .build();

        try (MockedConstruction<Product.Builder> mocked = Mockito.mockConstruction(Product.Builder.class, withSettings().defaultAnswer(RETURNS_SELF), (mock, context) -> {
            when(mock.build()).thenReturn(mockedResult); // Makes sure that build returns a valid product
        })) {
            setCurrentUser(ownerUser.getUserID());
            var productInfo = generateProductCreationInfo();

            assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                    .content(productInfo.toString())
                    .sessionAttrs(sessionAuthToken)
                    .contentType("application/json")
                    .cookie(authCookie))
                    .andExpect(status().isCreated())
                    .andReturn());

            // Checks that exactly 1 product builder was instantiated
            assertEquals(1, mocked.constructed().size());
            var mockBuilder = mocked.constructed().get(0);

            // Checks that the product builder was passed in the correct values
            verify(mockBuilder).withProductCode(productInfo.getAsString("id"));
            verify(mockBuilder).withName(productInfo.getAsString("name"));
            verify(mockBuilder).withDescription(productInfo.getAsString("description"));
            verify(mockBuilder).withManufacturer(productInfo.getAsString("manufacturer"));
            verify(mockBuilder).withRecommendedRetailPrice(productInfo.getAsString("recommendedRetailPrice"));
            verify(mockBuilder).build();
        }
    }

    /**
     * Tests that if Product.Builder.build returns a 400 then posting a product will also fail and return a 400.
     */
    @Test
    void postingProductFailsIfProductBuilderFails() {
        try (MockedConstruction<Product.Builder> ignored = Mockito.mockConstruction(Product.Builder.class, withSettings().defaultAnswer(RETURNS_SELF), (mock, context) ->
            when(mock.build()).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed for some reason"))
        )) {
            setCurrentUser(ownerUser.getUserID());
            var productInfo = generateProductCreationInfo();

            assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                    .content(productInfo.toString())
                    .sessionAttrs(sessionAuthToken)
                    .contentType("application/json")
                    .cookie(authCookie))
                    .andExpect(status().isBadRequest())
                    .andReturn());
        }
    }

    /**
     * Tests that a business admin can post a product
     */
    @Test
    void postingProductFromBusinessAdminAccount() {
        setCurrentUser(administratorUser.getUserID());

        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /**
     * Tests that the DGAA can post a product
     */
    @Test
    void postingProductFromDGAAAccount() {
        setCurrentUser(bystanderUser.getUserID());
        setUpDGAAAuthCode();

        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /**
     * Tests that the GAA can post a product
     */
    @Test
    void postingProductFromGAAAccount() {
        setCurrentUser(bystanderUser.getUserID());
        setUpSessionAsAdmin();

        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /**
     * Tests that the user that is not a DGAA, GAA, owner, business admin cannot post a product
     */
    @Test
    void postingProductFromBystanderAccount() {
        setCurrentUser(bystanderUser.getUserID());

        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isForbidden())
                .andReturn());
    }
    /**
     * Checks that the API returns products in the business's catalogue in default (product code) order.
     */
    @Test
    void retrieveCatalogueWithDefaultOrder() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        System.out.println(responseBody.toString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("ALMOND-MILK-100", firstProduct.getAsString("id"));
        assertEquals("NATHAN-APPLE-70", lastProduct.getAsString("id"));
    }


    /**
     * Checks that the API returns products in the business's catalogue in reverse order.
     */
    @Test
    void retrieveCatalogueWithReversing() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("reverse", "true"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("NATHAN-APPLE-70", firstProduct.getAsString("id"));
        assertEquals("ALMOND-MILK-100", lastProduct.getAsString("id"));
    }


    /**
     * Checks that the API returns the first page of paginated products in the businesses catalogue.
     */
    @Test
    void retrievePaginatedCatalogueFirstPage() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("page", "1")
                .param("resultsPerPage", "2"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        // Check length should be 2 products
        assertEquals(2, responseBody.size());

        // Check the two products are the expected ones
        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject secondProduct = (JSONObject) responseBody.get(1);

        assertEquals("ALMOND-MILK-100", firstProduct.getAsString("id"));
        assertEquals("COFFEE-7", secondProduct.getAsString("id"));
    }


    /**
     * Checks that the API returns the second page of paginated products in the businesses catalogue.
     */
    @Test
    void retrievePaginatedCatalogueSecondPage() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("page", "2")
                .param("resultsPerPage", "2"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        // Check length should be 2 products
        assertEquals(2, responseBody.size());

        // Check the two products are the expected ones
        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject secondProduct = (JSONObject) responseBody.get(1);

        assertEquals("DARK-CHOCOLATE", firstProduct.getAsString("id"));
        assertEquals("NATHAN-APPLE-70", secondProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by ID.
     */
    @Test
    void retrieveCatalogueOrderedById() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "id"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("ALMOND-MILK-100", firstProduct.getAsString("id"));
        assertEquals("NATHAN-APPLE-70", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by Name.
     */
    @Test
    void retrieveCatalogueOrderedByName() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "name"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("ALMOND-MILK-100", firstProduct.getAsString("id"));
        assertEquals("NATHAN-APPLE-70", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by Description.
     */
    @Test
    void retrieveCatalogueOrderedByDescription() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "description"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("NATHAN-APPLE-70", firstProduct.getAsString("id"));
        assertEquals("DARK-CHOCOLATE", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by Manufacturer.
     */
    @Test
    void retrieveCatalogueOrderedByManufacturer() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "manufacturer"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("NATHAN-APPLE-70", firstProduct.getAsString("id"));
        assertEquals("DARK-CHOCOLATE", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by Recommended Retail Price.
     */
    @Test
    void retrieveCatalogueOrderedByRRP() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "recommendedRetailPrice"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("COFFEE-7", firstProduct.getAsString("id"));
        assertEquals("NATHAN-APPLE-70", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns products correctly ordered by Created.
     */
    @Test
    void retrieveCatalogueOrderedByCreated() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .param("orderBy", "created"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("NATHAN-APPLE-70", firstProduct.getAsString("id"));
        assertEquals("DARK-CHOCOLATE", lastProduct.getAsString("id"));
    }

    /**
     * Checks that the API returns the number of products in the catalogue correctly.
     */
    @Test
    void retrieveCatalogueCount() throws Exception {
        setCurrentUser(ownerUser.getUserID());
        addSeveralProductsToACatalogue();

        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d/products/count", testBusiness1.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        Number count = responseBody.getAsNumber("count");

        assertEquals(4, count);
    }
}