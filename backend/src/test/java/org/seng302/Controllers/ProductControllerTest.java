package org.seng302.Controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
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
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .withProductCode("NathanApple-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple1")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("AlmondMilk100")
                .withName("Almond Milk")
                .withDescription("Like water except bad for the environment")
                .withManufacturer("Apple2")
                .withRecommendedRetailPrice("10.02")
                .withBusiness(testBusiness1)
                .build();
        Product product3 = new Product.Builder()
                .withProductCode("Coffee7")
                .withName("Generic Brand Coffee")
                .withDescription("This coffee tastes exactly as you expect it would")
                .withManufacturer("Apple3")
                .withRecommendedRetailPrice("4.02")
                .withBusiness(testBusiness1)
                .build();
        Product product4 = new Product.Builder()
                .withProductCode("DarkChocolate")
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

            assertEquals(storedProduct.getProductCode(), productCode);
            assertEquals(storedProduct.getCreated().toString().substring(0, 10), productJSON.getAsString("created").substring(0, 10));
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
     * Checks that the API returns products in the business's catalogue in default (product code) order.
     */
    @Test
    void retrieveCatalogueWithDefaultOrder() throws Exception {
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

        System.out.println(responseBody.toString());

        JSONObject firstProduct = (JSONObject) responseBody.get(0);
        JSONObject lastProduct = (JSONObject) responseBody.get(3);

        assertEquals("AlmondMilk100", firstProduct.getAsString("id"));
        assertEquals("NathanApple-70", lastProduct.getAsString("id"));
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

        assertEquals("NathanApple-70", firstProduct.getAsString("id"));
        assertEquals("AlmondMilk100", lastProduct.getAsString("id"));
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

        assertEquals("AlmondMilk100", firstProduct.getAsString("id"));
        assertEquals("Coffee7", secondProduct.getAsString("id"));
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

        assertEquals("DarkChocolate", firstProduct.getAsString("id"));
        assertEquals("NathanApple-70", secondProduct.getAsString("id"));
    }


    /**
     * Checks that the API returns paginated products in the businesses catalogue.
     */
    @Test
    void retrieveCatalogueOrderedByKey() throws Exception {
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

        assertEquals("Coffee7", firstProduct.getAsString("id"));
        assertEquals("NathanApple-70", lastProduct.getAsString("id"));
    }
}
