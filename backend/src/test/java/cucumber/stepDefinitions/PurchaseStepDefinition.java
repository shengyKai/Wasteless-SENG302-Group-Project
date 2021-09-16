package cucumber.stepDefinitions;

import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class PurchaseStepDefinition {

    private UserContext userContext;
    private BusinessContext businessContext;
    private SaleItemRepository saleItemRepository;
    private InventoryItemRepository inventoryItemRepository;
    private RequestContext requestContext;

    @Given("user {string} has purchased the sale listing {string} from business {string}")
    public void user_has_purchased_the_sale_listing_from_business(String userName, String itemName, String businessName) throws JSONException {
        var user = userContext.getByName(userName);
        var business = businessContext.getByName(businessName);
        var pageRequest = PageRequest.of(1, Math.toIntExact(saleItemRepository.count()));
        var saleItem = saleItemRepository.findAllForBusiness(business, pageRequest).stream()
                .filter(i -> i.getProduct().getName().equals(itemName)).collect(Collectors.toList()).get(0);
        var requestBody = new JSONObject();
        requestBody.put("purchaserId", user.getUserID());
        requestContext.performRequest(put(String.format("listings/%d/purchase", saleItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()));
    }

    @Then("the remaining quantity of the inventory item {string} will be {int}")
    public void the_remaining_quantity_of_the_inventory_item_will_be(String itemName, Integer remainingQuantity) {
        var inventoryItem = inventoryItemRepository.findAllForBusiness(businessContext.getLast()).stream()
                .filter(i -> i.getProduct().getName().equals(itemName)).collect(Collectors.toList()).get(0);
        Assertions.assertEquals(remainingQuantity, inventoryItem.getQuantity());
    }

    @When("I am viewing the sale listings for business {string}")
    public void i_am_viewing_the_sale_listings_for_business(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("the item {string} is not present")
    public void the_item_is_not_present(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("A record of the purchase is added to the business's sale history")
    public void a_record_of_the_purchase_is_added_to_the_business_s_sale_history() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I try to purchase the most recent sale listing")
    public void i_try_to_purchase_the_most_recent_sale_listing() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


}
