package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.seng302.entities.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InventoryStepDefinition {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;

    private Product product;
    private User owner;
    private User bystander;
    private Business business;
    private InventoryItem inventoryItem;
    private MvcResult ownerResult;
    private MvcResult bystanderResult;

    @Before
    public void Setup() {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
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

    private void loginAs(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    @Given("A business exists with one inventory item and one administrator")
    public void businesses_exists_with_inventory_and_administrator() throws Exception {
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        owner = userRepository.save(owner);
        business = new Business.Builder()
                .withName("Some Business")
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(owner)
                .build();
        business = businessRepository.save(business);
        product = new Product.Builder()
                .withProductCode("BEANS")
                .withBusiness(business)
                .withDescription("A product")
                .withManufacturer("Some stuff")
                .build();
        product = productRepository.save(product);
        inventoryItem = new InventoryItem.Builder()
                .withProduct(product)
                .withExpires("02/08/2021")
                .withQuantity(5)
                .build();
        inventoryItem = inventoryItemRepository.save(inventoryItem);
        bystander = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        bystander = userRepository.save(bystander);
    }

    @When("The business administrator and regular user try to fetch inventory data")
    public void business_admin_and_regular_user_fetch_inventory_data() throws Exception {
        loginAs(owner.getUserID());
        ownerResult = mockMvc.perform(get(String.format("businesses/%s/inventory", business.getId()))
        .sessionAttrs(sessionAuthToken)
        .cookie(authCookie)).andReturn();

        loginAs(bystander.getUserID());
        bystanderResult = mockMvc.perform(get(String.format("businesses/%s/inventory", business.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)).andReturn();
    }
    @Then("The business administrator should be able to view the inventory and the regular user should receive an error")
    public void business_admin_can_view_inventory_and_regular_user_cannot() {
        Assert.assertEquals(404, ownerResult.getResponse().getStatus());
        Assert.assertEquals(403, bystanderResult.getResponse().getStatus());
    }
}
