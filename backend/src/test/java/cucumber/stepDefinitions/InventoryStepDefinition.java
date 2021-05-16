package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import org.seng302.entities.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class InventoryStepDefinition  {
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
    private User actor;
    private Business business;
    private MvcResult mvcResult;

    @Given("a business exists")
    public void a_business_exists() throws ParseException {
        User owner = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("owner@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        owner = userRepository.save(owner);
        business = new Business.Builder()
                .withName("Business")
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(owner)
                .build();
        business = businessRepository.save(business);
    }

    @Given("the business has the following products in its catalogue:")
    public void the_business_has_the_following_products_in_its_catalogue(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Product product = new Product.Builder()
                    .withProductCode(row.get("product_id"))
                    .withName(row.get("name"))
                    .withBusiness(business)
                    .build();
            business.addToCatalogue(product);
            productRepository.save(product);
        }
    }

    @Given("the business has the following items in its inventory:")
    public void the_business_has_the_following_items_in_its_inventory(io.cucumber.datatable.DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(business, row.get("product_id"));
            if (product.isEmpty()) {
                throw new Exception("Product should be saved to product repository");
            }
            InventoryItem item = new InventoryItem.Builder()
                    .withProduct(product.get())
                    .withQuantity(Integer.parseInt(row.get("quantity")))
                    .withExpires(row.get("expires"))
                    .build();
            inventoryItemRepository.save(item);
        }
    }

    @Given("I am an administrator of the business")
    public void i_am_an_administrator_of_the_business() throws ParseException {
        actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        actor = userRepository.save(actor);
        business.addAdmin(actor);
        business = businessRepository.save(business);
        boolean checkAdmin = false;
        for (User user : business.getAdministrators()) {
            if (user.getUserID().equals(actor.getUserID())) {
                checkAdmin = true;
                break;
            }
        }
        assertTrue(checkAdmin);
    }

    @Given("I am an not an administrator of the business")
    public void i_am_an_not_an_administrator_of_the_business() throws ParseException {
        actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        actor = userRepository.save(actor);
        for (User user : business.getAdministrators()) {
            assertNotEquals(user.getUserID(), actor.getUserID());
        }
    }

    @Given("I am logged into my account")
    public void i_am_logged_into_my_account() {
        String authCode = "0".repeat(64);
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
        sessionAuthToken.put("accountId", actor.getUserID());
    }

    @When("I try to access the inventory of the business")
    public void i_try_to_access_the_inventory_of_the_business() throws Exception {
        mvcResult = mockMvc.perform(get(String.format("/businesses/%s/inventory", business.getId()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)).andReturn();
    }

    @Then("the inventory of the business is returned to me")
    public void the_inventory_of_the_business_is_returned_to_me() throws UnsupportedEncodingException, JsonProcessingException {
        assertEquals(200, mvcResult.getResponse().getStatus());

        List<Product> catalogue = productRepository.findAllByBusiness(business);
        List<InventoryItem> inventory = inventoryItemRepository.getInventoryByCatalogue(catalogue);
        JSONArray jsonArray = new JSONArray();
        for (InventoryItem item : inventory) {
            jsonArray.appendElement(item.constructJSONObject());
        }
        String expectedResponse = jsonArray.toJSONString();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertEquals(objectMapper.readTree(expectedResponse), objectMapper.readTree(responseBody));
    }

    @Then("I cannot view the inventory")
    public void i_cannot_view_the_inventory() throws UnsupportedEncodingException {
        assertEquals(403, mvcResult.getResponse().getStatus());
        assertEquals("", mvcResult.getResponse().getContentAsString());
    }
}
