package org.seng302.Controllers;

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
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
import org.seng302.Entities.Product;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.ProductRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class ProductControllerTest {

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
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
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
                .withDob("2010-03-11")
                .withPhoneNumber("+64 5 565 0129")
                .withAddress(Location.covertAddressStringToLocation("5,Rountree Street,Christchurch,New Zealand," +
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
                .withAddress(Location.covertAddressStringToLocation("5,Rountree Street,Christchurch,New Zealand," +
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
     * Tests that using the POST /businesses/:id/products endpoint adds a product to the given businesses product
     * catalogue
     */
    @Test
    void postingAProductAddsItToTheCatalogue() {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isCreated())
                .andReturn());

        Product product =
                productRepository.findByBusinessAndProductCode(testBusiness1, productInfo.getAsString("id"));

        assertNotNull(product);
        assertEquals(productInfo.get("id"), product.getProductCode());
        assertEquals(productInfo.get("name"), product.getName());
        assertEquals(productInfo.get("description"), product.getDescription());
        assertEquals(productInfo.get("manufacturer"), product.getManufacturer());
        // There is a small fudge factor between the exact decimal value and the closest double representation of said value
        assertEquals(
                (double)productInfo.get("recommendedRetailPrice"),
                product.getRecommendedRetailPrice().doubleValue(),
                1e-10
        );
    }

    /**
     * Tests that when a product is added via the POST /businesses/:id/products endpoint that the created product's
     * creation time is between the start of the request and the end of the request
     */
    @Test
    void postingAProductSetsTheCreationTimeCorrectly() {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        Date before = new Date();
        MvcResult result = assertDoesNotThrow(() ->
                mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                    .content(productInfo.toString())
                    .sessionAttrs(sessionAuthToken)
                    .contentType("application/json")
                    .cookie(authCookie))
                    .andExpect(status().isCreated())
                    .andReturn()
            );
        Date after = new Date();

        Product product =
                productRepository.findByBusinessAndProductCode(testBusiness1, productInfo.getAsString("id"));

        assertNotNull(product);
        // Asserts that the product is created not before the production creation should have finished
        assertFalse(after.before(product.getCreated()));
        // Asserts that the product is created not after the production creation shouldn't have started
        assertFalse(before.after(product.getCreated()));
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
                .andExpect(status().isBadRequest())
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
     * Tests that if any field in the post product request is not included then a 400 response is sent.
     * @param key The field to remove
     */
    @ParameterizedTest
    @ValueSource(strings = {"id", "name"})
    void postingProductWithMissingField(String key) {
        setCurrentUser(ownerUser.getUserID());
        var productInfo = generateProductCreationInfo();

        productInfo.remove(key);

        assertDoesNotThrow(() -> mockMvc.perform(post(String.format("/businesses/%d/products", testBusiness1.getId()))
                .content(productInfo.toString())
                .sessionAttrs(sessionAuthToken)
                .contentType("application/json")
                .cookie(authCookie))
                .andExpect(status().isBadRequest())
                .andReturn());
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
}
