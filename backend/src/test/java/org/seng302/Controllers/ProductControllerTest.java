package org.seng302.Controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    UserRepository userRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private Business testBusiness1;
    private User testUser1;

    /**
     * Add a user object to the userRepository and construct an authorization token to be used for this session.
     *
     * @throws ParseException
     */
    @BeforeEach
    public void setUpIndividual() throws ParseException, IOException {
        setUpAuthCode();
    }

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode() {
        StringBuilder authCodeBuilder = new StringBuilder();
        authCodeBuilder.append("0".repeat(64));
        String authCode = authCodeBuilder.toString();
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }

    /**
     * Tags a session as dgaa
     */
    private void setUpDGAAAuthCode() {
        sessionAuthToken.put("role", "defaultGlobalApplicationAdmin");
    }
    private void setUpSessionAsAdmin() {
        sessionAuthToken.put("role", "globalApplicationAdmin");
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
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);
    }

    @BeforeAll
    public void setUp() throws ParseException {
        productRepository.deleteAll();
        businessRepository.deleteAll();

        testUser1 = new User.Builder()
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
        testUser1 = userRepository.save(testUser1);
        createTestBusiness();
    }

    /**
     * Adds several products to a catalogue
     */
    public void addSeveralProductsToACatalogue() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-70").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1).build();
        Product product2 = new Product.Builder().withProductCode("AlmondMilk100").withName("Almond Milk")
                .withDescription("Like water except bad for the environment").withRecommendedRetailPrice("10.02")
                .withBusiness(testBusiness1).build();
        Product product3 = new Product.Builder().withProductCode("Coffee7").withName("Generic Brand Coffee")
                .withDescription("This coffee tastes exactly as you expect it would").withRecommendedRetailPrice("4.02")
                .withBusiness(testBusiness1).build();
        Product product4 = new Product.Builder().withProductCode("DarkChocolate").withName("Dark Chocolate")
                .withDescription("Would like a high cocoa concentration").withRecommendedRetailPrice("6.07")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        productRepository.save(product4);
    }

    /**
     * //TODO Write docstring
     */
    @Test
    void retrieveCatalogueWithSeveralProducts() throws Exception {
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
        }
    }

    /**
     * //TODO Write docstring
     */
    @Test
    void retrieveCatalogueWithZeroProducts() {

    }

    /**
     * //TODO Write docstring
     */
    @Test
    void invalidAuthTokenThenCannotViewCatalogue() {

    }

    /**
     * //TODO Write docstring
     */
    @Test
    void businessDoesNotExistThenNotAccepted() {

    }

    /**
     * //TODO Write docstring
     */
    @Test
    public void userIsNotAnAdminThenForbidden() {

    }

    /**
     * //TODO Write docstring
     */
    @Test
    void userIsAnAdminCanRetrieveCatalogue() {

    }

    /**
     * //TODO Write docstring
     */
    @Test
    void userIsADGAACanRetrieveCatalogue() {

    }

    /**
     * Tests to create:
     *
     * - Retrieve a catalogue with several products (code 200)
     * - Retrieve a catalogue with zero products (code 200)
     * - Check the when an invalid auth token is provided permission is denied (code 401)
     * - Check when the business does not exist there a exception thrown (code 406)
     * - Check when the user is not an admin a forbidden is thrown (code 403)
     * - Check a DGAA can retrieve catalogues
     */


}
