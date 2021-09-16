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
    private Boolean reverse = false;


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
        assertEquals(200, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray sales = (JSONArray) response.get("results");

        assertTrue(sales.size() > 0);
        JSONObject saleItem = (JSONObject) sales.get(0);
        JSONObject inventoryItem = (JSONObject) saleItem.get("inventoryItem");
        JSONObject product = (JSONObject) inventoryItem.get("product");
        assertEquals(firstId, product.get("id"));
    }

    @Then("first product is from {string}")
    public void productsAreInBusinessOrder(String businessName) throws UnsupportedEncodingException, ParseException {
        mvcResult = requestContext.getLastResult();
        assertEquals(200, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray sales = (JSONArray) response.get("results");

        assertTrue(sales.size() > 0);
        JSONObject saleItem = (JSONObject) sales.get(0);
        JSONObject inventoryItem = (JSONObject) saleItem.get("inventoryItem");
        JSONObject product = (JSONObject) inventoryItem.get("product");
        JSONObject business = (JSONObject) product.get("business");
        assertEquals(businessName, business.getAsString("name"));
    }

    @When("businessType is {string}")
    public void businesstypeIs(String type) { this.businessTypes = type; }

    @When("search sale name is {string}")
    public void searchSaleNameIs(String name) { this.productSearch = name; }

    @When("search sale price is between {string} and {string}")
    public void searchSalePriceIsBetweenAnd(String priceLower, String priceUpper) {
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
        requestContext.performRequest(get("/businesses/listings/search")
                .param("basicSearchQuery", this.basicSearch)
                .param("productSearchQuery", this.productSearch)
                .param("businessSearchQuery", this.businessSearch)
                .param("locationSearchQuery", this.locationSearch)
                .param("orderBy", this.orderBy)
                .param("businessTypes", this.businessTypes)
                .param("priceLower", this.priceLower)
                .param("priceUpper", this.priceUpper)
                .param("closeLower", this.closeLower)
                .param("closeUpper", this.closeUpper)
        );
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
        this.reverse = false;
    }

    @When("I search sale basic {string}")
    public void genericSearch(String search) { this.basicSearch = search; }
}
