package cucumber.stepDefinitions;

import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Assert;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class BusinessStepDefinition {
    @Autowired
    private BusinessContext businessContext;

    @Autowired
    private UserContext userContext;
    @Autowired
    private RequestContext requestContext;

    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {

        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(userContext.getLast())
                .build();
        businessContext.save(business);
    }

    @When("I search with query {string}")
    public void searchBusiness(String query) throws Exception {
        requestContext.performRequest(get("/businesses/search")
        .param("searchQuery", query));
    }
    @Then("I expect business {string} to be returned")
    public void businessIsReturned(String name) throws Exception {
        MvcResult result = requestContext.getLastResult();
        Assert.assertEquals(200, result.getResponse().getStatus());
        Business expectedBusiness = businessContext.getByName(name);
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray businesses = (JSONArray) response.get("results");
        boolean found = false;
        for (Object business : businesses) {
            Number id = ((JSONObject) business).getAsNumber("id");
            if (expectedBusiness.getId().equals(id.longValue())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
    }

    @Then("I don't expect business {string} to be returned")
    public void businessIsNotReturned(String name) throws Exception {
        MvcResult result = requestContext.getLastResult();
        Assert.assertEquals(200, result.getResponse().getStatus());
        Business expectedBusiness = businessContext.getByName(name);
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray businesses = (JSONArray) response.get("results");
        boolean found = false;
        for (Object business : businesses) {
            Number id = ((JSONObject) business).getAsNumber("id");
            if (expectedBusiness.getId().equals(id.longValue())) {
                found = true;
                break;
            }
        }
        Assert.assertFalse(found);
    }
}
