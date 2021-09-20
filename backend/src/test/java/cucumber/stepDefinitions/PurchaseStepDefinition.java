package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.BoughtSaleItem;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.persistence.BoughtSaleItemRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PurchaseStepDefinition {

    @Autowired
    private UserContext userContext;
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private BoughtSaleItemRepository boughtSaleItemRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Given("user {string} has purchased the sale listing {string} from business {string}")
    public void user_has_purchased_the_sale_listing_from_business(String userName, String itemName, String businessName) throws JSONException {
        var user = userContext.getByName(userName);
        requestContext.setLoggedInAccount(user);

        var saleItem = StreamSupport.stream(saleItemRepository.findAll().spliterator(), false)
                .filter(i -> i.getProduct().getName().equals(itemName) && i.getBusiness().getName().equals(businessName))
                .collect(Collectors.toList()).get(0);

        var requestBody = new JSONObject();
        requestBody.put("purchaserId", user.getUserID());

        requestContext.performRequest(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()));
    }

    @Then("the quantity of the inventory item {string} will be {int}")
    public void the_quantity_of_the_inventory_item_will_be(String itemName, Integer remainingQuantity) {
        var inventoryItem = inventoryItemRepository.findAllForBusiness(businessContext.getLast()).stream()
                .filter(i -> i.getProduct().getName().equals(itemName)).collect(Collectors.toList()).get(0);
        Assertions.assertEquals(remainingQuantity, inventoryItem.getQuantity());
    }

    @When("I am viewing the sale listings for business {string}")
    public void i_am_viewing_the_sale_listings_for_business(String businessName) {
        var business = businessContext.getByName(businessName);
        requestContext.performRequest(get(String.format("/businesses/%d/listings", business.getId())));
    }

    @Then("the item {string} is not present")
    public void the_item_is_not_present(String name) throws JsonProcessingException {
        var response = requestContext.getLastResultAsString();
        var responseJson = objectMapper.readValue(response, JsonNode.class);
        Assertions.assertTrue(responseJson.has("results"));
        var results = responseJson.get("results");
        Assertions.assertEquals(JsonNodeType.ARRAY, results.getNodeType());
        for (int i = 0; i < results.size(); i++) {
            var saleJson = results.get(i);
            var saleName = saleJson.get("inventoryItem").get("product").get("name").asText();
            Assertions.assertNotEquals(name, saleName);
        }
    }

    @Transactional
    @Then("A record of the purchase is added to the business's sale history")
    public void a_record_of_the_purchase_of_item_is_added_to_the_business_s_sale_history() {
        var business = businessContext.getLast();
        var user = userContext.getLast();

        var boughtSaleItem = StreamSupport.stream(boughtSaleItemRepository.findAll().spliterator(), false)
                .filter(b -> b.getProduct().getBusiness().equals(business))
                .max(Comparator.comparing(BoughtSaleItem::getSaleDate))
                .orElseThrow();

        Assertions.assertEquals(user.getUserID(), boughtSaleItem.getBuyer().getUserID());
    }

    @Transactional
    @When("I try to purchase the most recent sale listing")
    public void i_try_to_purchase_the_most_recent_sale_listing() throws JSONException {
        var user = userContext.getLast();
        var business = businessContext.getLast();

        var saleItem = StreamSupport.stream(saleItemRepository.findAll().spliterator(), false)
                .filter(s -> s.getBusiness().getName().equals(business.getName()))
                .max(Comparator.comparing(SaleItem::getCreated)).orElseThrow();

        var requestBody = new JSONObject();
        requestBody.put("purchaserId", user.getUserID());

        requestContext.performRequest(post(String.format("/listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()));
    }


}
