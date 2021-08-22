package cucumber.stepDefinitions;

import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class ProductSearchStepDefinition {

    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestContext requestContext;

    private MvcResult mvcResult;
    private List<String> options = new ArrayList<>();

    @When("I search the catalogue for {string}")
    public void iSearchTheCatalogueFor(String search) {
        MockHttpServletRequestBuilder requestBuilder = get(String.format("/businesses/%s/products/search", businessContext.getLast().getId()))
                .param("searchQuery", search);
        for (String option : options) {
            requestBuilder.param("searchBy", option);
        }
        requestContext.performRequest(requestBuilder);
    }

    @Then("{int} products are returned")
    public void productsAreReturned(Integer prodCount) throws UnsupportedEncodingException, ParseException {
        mvcResult = requestContext.getLastResult();
        assertEquals(200, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray products = (JSONArray) response.get("results");
        assertEquals((int) prodCount, products.size());
    }

    @And("No product fields are selected")
    public void noProductFieldsAreSelected() {
        options.clear();
    }

    @Given("product fields are selected:")
    public void productFieldsAreSelected(List<String> fields) {
        options = fields;
    }
}
