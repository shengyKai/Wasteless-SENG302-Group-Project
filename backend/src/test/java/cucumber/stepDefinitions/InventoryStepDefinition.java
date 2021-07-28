package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class InventoryStepDefinition  {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private RequestContext requestContext;

    private MvcResult mvcResult;
    private String productCode;
    private Integer quantity;

    @After
    public void cleanUp() {
        productRepository.deleteAll();
        inventoryItemRepository.deleteAll();
    }

    @Given("the business has the following products in its catalogue:")
    public void the_business_has_the_following_products_in_its_catalogue(io.cucumber.datatable.DataTable dataTable) {
        var business = businessContext.getLast();
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
        businessContext.save(business);
    }

    @Given("the business has the following items in its inventory:")
    public void the_business_has_the_following_items_in_its_inventory(io.cucumber.datatable.DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), row.get("product_id"));
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
        var actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userContext.save(actor);

        var business = businessContext.getLast();
        business.addAdmin(actor);
        business = businessContext.save(business);

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
        var actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        actor = userContext.save(actor);
        for (User user : businessContext.getLast().getAdministrators()) {
            assertNotEquals(user.getUserID(), actor.getUserID());
        }
    }

    @When("I try to access the inventory of the business")
    public void i_try_to_access_the_inventory_of_the_business() throws Exception {
        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        get(String.format("/businesses/%s/inventory", businessContext.getLast().getId()))
                )
        ).andReturn();
    }

    @Then("the inventory of the business is returned to me")
    public void the_inventory_of_the_business_is_returned_to_me() throws UnsupportedEncodingException, JsonProcessingException {
        assertEquals(200, mvcResult.getResponse().getStatus());

        List<InventoryItem> inventory = inventoryItemRepository.findAllForBusiness(businessContext.getLast());

        //because now the inventory sorts by product code on default, so it has to be sorted before comparing
        Comparator<InventoryItem> sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getProductCode());
        inventory.sort(sort);

        JSONObject expectedPage = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (InventoryItem item : inventory) {
            jsonArray.appendElement(item.constructJSONObject());
        }
        expectedPage.put("results", jsonArray);
        expectedPage.put("count", jsonArray.size());
        String expectedResponse = expectedPage.toJSONString();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertEquals(objectMapper.readTree(expectedResponse), objectMapper.readTree(responseBody));
    }

    @Then("I cannot view the inventory")
    public void i_cannot_view_the_inventory() throws UnsupportedEncodingException {
        assertEquals(403, mvcResult.getResponse().getStatus());
        assertEquals("", mvcResult.getResponse().getContentAsString());
    }

    @When("I create an inventory item with product code {string} and quantity {int} and expiry {string}")
    public void i_create_inventory_with_product_code_quantity_string(String productCode, Integer quantity, String expiry) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"," +
                    "\"quantity\": %d," +
                    "\"expires\": \"%s\"" +
                "}"
        , productCode, quantity, expiry);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                )
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string} and quantity {int}, expiry {string}, price per item {int} and total price {int}")
    public void i_create_inventory_with_all_details(String productCode, Integer quantity, String expiry, Integer pricePerItem, Integer totalPrice) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"," +
                    "\"quantity\": %d," +
                    "\"expires\": \"%s\"," +
                    "\"pricePerItem\": %d," +
                    "\"totalPrice\": %d" +
                "}"
                , productCode, quantity, expiry, pricePerItem, totalPrice);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string}, quantity {int}, expiry {string}, manufactured on {string}, sell by {string} and best before {string}")
    public void i_create_inventory_with_all_dates(String productCode, Integer quantity, String expiry, String manufactured, String sellBy, String bestBefore) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                        "\"productId\": \"%s\"," +
                        "\"quantity\": %d," +
                        "\"expires\": \"%s\"," +
                        "\"manufactured\": \"%s\"," +
                        "\"sellBy\": \"%s\"," +
                        "\"bestBefore\": \"%s\"" +
                        "}"
                , productCode, quantity, expiry, manufactured, sellBy, bestBefore);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string} and no other fields")
    public void i_create_inventory_item_without_required_fields(String productCode) throws Exception {
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"" +
                "}"
                , productCode);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("I expect to be prevented from creating the inventory item")
    public void i_expect_to_be_prevented_from_creating_inventory_item() {
        assertEquals(400, mvcResult.getResponse().getStatus());
        Product product = productRepository.findAllByBusiness(businessContext.getLast()).stream().filter(x -> x.getProductCode().equals(this.productCode)).collect(Collectors.toList()).get(0);
        List<InventoryItem> inventory = inventoryItemRepository.findAllByProduct(product);
        assertEquals(1, inventory.size());

    }

    @Then("I expect the inventory item to be created")
    public void i_expect_the_inventory_to_be_created() {
        assertEquals(200, mvcResult.getResponse().getStatus());
        Product product = productRepository.findAllByBusiness(businessContext.getLast()).stream().filter(x -> x.getProductCode().equals(this.productCode)).collect(Collectors.toList()).get(0);
        List<InventoryItem> inventory = inventoryItemRepository.findAllByProduct(product);
        assertTrue(inventory.stream().anyMatch(x-> x.getQuantity() == quantity));
    }

    /**
     * Generates a mock JSON inventory item JSON body to be used in the modify inventory entries API endpoint
     * @return inventory item JSON body
     */
    public JSONObject generateInvJSONBody() {
        JSONObject invBody = new JSONObject();
        invBody.put("productId", "APPLE11");
        invBody.put("quantity", 10);
        invBody.put("pricePerItem", 5.42);
        invBody.put("totalPrice", 54.20);
        invBody.put("manufactured", LocalDate.now().minusYears(100).toString());
        invBody.put("sellBy", LocalDate.now().plusYears(100).toString());
        invBody.put("bestBefore", LocalDate.now().plusYears(200).toString());
        invBody.put("expires", LocalDate.now().plusYears(300).toString());
        return invBody;
    }

    @Given("the business has the following list of items in its inventory:")
    public void the_business_has_the_following_list_of_items_in_its_inventory(io.cucumber.datatable.DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), row.get("product_id"));
            if (product.isEmpty()) {
                throw new Exception("Product should be saved to product repository");
            }
            InventoryItem item = new InventoryItem.Builder()
                    .withProduct(product.get())
                    .withQuantity(Integer.parseInt(row.get("quantity")))
                    .withPricePerItem(row.get("price_per_item"))
                    .withTotalPrice(row.get("total_price"))
                    .withManufactured(row.get("manufactured"))
                    .withSellBy(row.get("sellBy"))
                    .withBestBefore(row.get("bestBefore"))
                    .withExpires(row.get("expires"))
                    .build();
            inventoryItemRepository.save(item);

            //Changing Inventory ID
            long actualId = item.getId();
            long wantedId = Long.parseLong(row.get("id"));
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE inventory_item SET id = ? WHERE id = ?"
                );
                stmt.setLong(1, wantedId);
                stmt.setLong(2, actualId);
                stmt.executeUpdate();
            });
        }
    }

    @When("I try to modify the quantity to {int} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_quantity_to_for_the_inventory_entry_with_the_version(
            int quantity, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("quantity");
        invBody.put("quantity", quantity);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the price per item to {float} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_price_per_item_to_for_the_inventory_entry_with_the_version(
            float pricePerItem, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("pricePerItem");
        invBody.put("pricePerItem", pricePerItem);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the total price to {float} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_total_price_to_for_the_inventory_entry_with_the_version(
            float totalPrice, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("totalPrice");
        invBody.put("totalPrice", totalPrice);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the manufactured date to {string} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_manufacutured_date_to_for_the_inventory_entry_with_the_version(
            String manufactured, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("manufactured");
        invBody.put("manufactured", manufactured);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the sell by date to {string} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_sell_by_date_to_for_the_inventory_entry_with_the_version(
            String sellBy, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("sellBy");
        invBody.put("sellBy", sellBy);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the best before date to {string} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_best_before_date_to_for_the_inventory_entry_with_the_version(
            String bestBefore, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("bestBefore");
        invBody.put("bestBefore", bestBefore);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the expires date to {string} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_expires_date_to_for_the_inventory_entry_with_the_version(
            String expires, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("expires");
        invBody.put("expires", expires);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the product to the one with the product code {string} for the inventory entry with the id {long}")
    public void i_try_to_modify_the_product_to_the_one_with_the_product_code_for_the_inventory_entry_with_the_version(
            String productCode, long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("productCode");
        invBody.put("productCode", productCode);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the quantity to null for the inventory entry with the id {long}")
    public void i_try_to_modify_the_quantity_to_null_for_the_inventory_entry_with_the_version(
            long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("quantity");
        invBody.put("quantity", null);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the expires date to null for the inventory entry with the id {long}")
    public void i_try_to_modify_the_expires_date_to_null_for_the_inventory_entry_with_the_version(
            long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("expires");
        invBody.put("expires", null);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I try to modify the manufactured date to null for the inventory entry with the id {long}")
    public void i_try_to_modify_the_manufactured_date_to_null_for_the_inventory_entry_with_the_version(
            long invItemId) throws Exception {
        JSONObject invBody = generateInvJSONBody();
        invBody.remove("expires");
        invBody.put("expires", null);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        put(String.format("/businesses/%d/inventory/%d", businessContext.getLast().getId(), invItemId))
                ).content(invBody.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("the quantity of the inventory item with the id {long} will be {int}")
    public void the_quantity_of_the_inventory_item_with_the_version_will_be(
            long invItemId, int quantity) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getQuantity(), quantity);
    }

    @Then("the price per item of the inventory item with the id {long} will be {double}")
    public void the_price_per_item_of_the_inventory_item_with_the_version_will_be(
            long invItemId, double pricePerItem) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        BigDecimal pricePerItemBD = BigDecimal.valueOf(pricePerItem);
        pricePerItemBD = pricePerItemBD.setScale(2, RoundingMode.HALF_UP);
        assertEquals(invItem.getPricePerItem(), pricePerItemBD);
    }

    @Then("the total price of the inventory item with the id {long} will be {double}")
    public void the_total_price_of_the_inventory_item_with_the_version_will_be(
            long invItemId, double totalPrice) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        BigDecimal totalPriceBD = BigDecimal.valueOf(totalPrice);
        totalPriceBD = totalPriceBD.setScale(2, RoundingMode.HALF_UP);
        assertEquals(invItem.getTotalPrice(), totalPriceBD);
    }

    @Then("the manufactured date of the inventory item with the id {long} will be {string}")
    public void the_manufactured_date_of_the_inventory_item_with_the_version_will_be(
            long invItemId, String manufactured) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getManufactured().toString(), manufactured);
    }

    @Then("the manufactured date of the inventory item with the id {long} will be null")
    public void the_manufactured_date_of_the_inventory_item_with_the_version_will_be_null(
            long invItemId, String manufactured) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNull(invItem);
    }

    @Then("the sell by date of the inventory item with the id {long} will be {string}")
    public void the_sell_by_date_of_the_inventory_item_with_the_version_will_be(
            long invItemId, String sellBy) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getSellBy().toString(), sellBy);
    }

    @Then("the best before date of the inventory item with the id {long} will be {string}")
    public void the_best_before_date_of_the_inventory_item_with_the_version_will_be(
            long invItemId, String bestBefore) {
        System.out.println(mvcResult.getResponse().getErrorMessage());
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getBestBefore().toString(), bestBefore);
    }

    @Then("the expires date of the inventory item with the id {long} will be {string}")
    public void the_expires_date_of_the_inventory_item_with_the_version_will_be(
            long invItemId, String expires) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getExpires().toString(), expires);
    }

    @Then("the product of the inventory item with the id {long} will have the product code {string}")
    public void the_product_of_the_inventory_item_with_the_version_will_have_the_product_code(
            long invItemId, String productCode) {
        assertEquals(200, mvcResult.getResponse().getStatus());
        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                businessContext.getLast(), invItemId);
        assertNotNull(invItem);
        assertEquals(invItem.getProduct().getProductCode(), productCode);
    }

    @Then("I will receive a forbidden response")
    public void i_will_receive_a_forbidden_response() {
        assertEquals(403, mvcResult.getResponse().getStatus());
    }

    @Then("I will receive a bad request response")
    public void i_will_receive_a_bad_request_response() {
        assertEquals(400, mvcResult.getResponse().getStatus());
    }
}

















