package cucumber.stepDefinitions;

import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class SaleSearchStepDefinition {
    @Autowired
    private UserContext userContext;
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestContext requestContext;

    private MvcResult mvcResult;
    
    private String basicSearch = "";
    private String productSearch = "";
    private String businessSearch = "";
    private String locationSearch = "";
    private String orderBy = "";
    private String businessTypes = "";
    private String priceLower = "";
    private String priceUpper = "";
    private String closeLower = "";
    private String closeUpper = "";

    @And("the business {string} with the type {string} and location {string} exists")
    public void theBusinessWithTheTypeAndLocationExists(String name, String type, String location) {
        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType(type)
                .withAddress(Location.covertAddressStringToLocation(location))
                .withPrimaryOwner(userContext.getLast())
                .build();
        businessContext.save(business);
    }

    @And("the business has the following products on sale:")
    public void theBusinessHasTheFollowingProductsOnSale(io.cucumber.datatable.DataTable datatable) throws Exception {
        List<Map<String, String>> rows = datatable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), row.get("product_id"));
            if (product.isEmpty()) {
                throw new Exception("Product should be saved to product repository");
            }
            InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product.get()).get(0);

            SaleItem item = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withQuantity(Integer.parseInt(row.get("quantity")))
                    .withPrice(row.get("price"))
                    .withCloses(row.get("closes"))
                    .build();
            saleItemRepository.save(item);
        }
    }

    @Then("{int} sale items are returned")
    public void saleItemsAreReturned(int count) throws UnsupportedEncodingException, ParseException {
        mvcResult = requestContext.getLastResult();
        assertEquals(200, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray sales = (JSONArray) response.get("results");
        assertEquals(count, sales.size());
    }

    @When("orderBy is {string}")
    public void orderbyIs(String orderBy) { this.orderBy = orderBy; }

    @Then("first product is {string}")
    public void productsAreInAlphabeticalOrder(String firstId) throws UnsupportedEncodingException, ParseException {
        mvcResult = requestContext.getLastResult();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray sales = (JSONArray) response.get("results");

        assertTrue(sales.size() > 0);
        Number saleItemId = (Number) sales.getJSONObject(0).get("id");
        SaleItem saleItem = saleItemRepository.findById(saleItemId.longValue()).orElseThrow();
        assertEquals(firstId, saleItem.getInventoryItem().getProduct().getProductCode());
    }

    @Then("first product is from {string}")
    public void productsAreInBusinessOrder(String businessName) throws UnsupportedEncodingException, ParseException {
        mvcResult = requestContext.getLastResult();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray sales = (JSONArray) response.get("results");

        assertTrue(sales.size() > 0);
        Number businessId = (Number) sales.getJSONObject(0).get("business").get("name");
        assertEqual(businessName, business.getName())
    }

    @When("businessType is {string}")
    public void businesstypeIs(String type) { this.businessTypes = type; }

    @When("search sale name is {string}")
    public void searchSaleNameIs(String name) { this.productSearch = name; }

    @When("search sale price is between {int} and {int}")
    public void searchSalePriceIsBetweenAnd(int priceLower, int priceUpper) {
        this.priceLower = priceLower;
        this.priceUpper = priceUpper;
    }

    @When("search sale business is {string}")
    public void searchSaleBusinessIs(String busName) { this.businessSearch = busName; }

    @When("search sale location is {string}")
    public void searchSaleLocationIs(String location) { this.locationSearch = location; }

    @When("search sale date is between {string} and {string}")
    public void searchSaleDateIsBetweenAnd(String closeLower, String closeUpper) {
        this.closeLower = closeLower;
        this.closeUpper = closeUpper;
    }

    @When("I search for sale items")
    public void searchSaleItems() {
        MockHttpServletRequestBuilder requestBuilder = get("/businesses/listings/search")
                .param("basicSearchQuery", this.basicSearch)
                .param("productSearchQuery", this.productSearch)
                .param("businessSearchQuery", this.businessSearch)
                .param("locationSearchQuery", this.locationSearch)
                .param("orderBy", this.orderBy)
                .param("businessTypes", this.businessTypes)
                .param("priceLower", this.priceLower)
                .param("priceUpper", this.priceUpper)
                .param("closeLower", this.closeLower)
                .param("closeUpper", this.closeUpper);
        requestContext.performRequest(requestBuilder);
        reset();
    }

    private void reset() {
        this.basicSearch = "";
        this.productSearch = "";
        this.businessSearch = "";
        this.locationSearch = "";
        this.businessTypes = "";
        this.priceLower = "";
        this.priceUpper = "";
        this.closeLower = "";
        this.closeUpper = "";
    }

    @When("I search sale basic {string}")
    public void genericSearch(String search) { this.basicSearch = search; }
}
