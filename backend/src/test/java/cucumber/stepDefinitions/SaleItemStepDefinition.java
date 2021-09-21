package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.seng302.leftovers.dto.event.InterestEventDTO;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class SaleItemStepDefinition {
    @Autowired
    private UserContext userContext;
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;

    private Integer quantity;
    private Long inventoryItemId;
    private Double price;
    private String closing;
    private String moreInfo;

    private Set<SaleItem> addedSaleItems;

    @Given("the business is listing the following items")
    public void the_business_is_listing_the_following_items(io.cucumber.datatable.DataTable dataTable) throws Exception {
        addedSaleItems = new HashSet<>();
        requestContext.setLoggedInAccount(businessContext.getLast().getPrimaryOwner());

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), row.get("product_id"));
            if (product.isEmpty()) {
                throw new Exception("Product should be saved to product repository");
            }
            InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product.get()).get(0);

            var object = new JSONObject();
            object.put("inventoryItemId", inventoryItem.getId());
            object.put("quantity", row.get("quantity"));
            object.put("price", row.get("price"));

            requestContext.performRequest(
                    post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                            .content(object.toString())
                            .contentType(MediaType.APPLICATION_JSON));

            var item = StreamSupport.stream(saleItemRepository.findAll().spliterator(), false)
                    .max(Comparator.comparing(SaleItem::getCreated))
                    .orElseThrow();

            addedSaleItems.add(item);
        }
    }

    @When("I create a sale item for product code {string}, quantity {int}, price {double}")
    public void i_create_a_sale_item_for_product_code_quantity_price(String productCode, int quantity, double price) throws Exception {
        Product product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), productCode).orElseThrow();
        InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product).stream().findFirst().orElseThrow();
        this.inventoryItemId = inventoryItem.getId();
        this.quantity = quantity;
        this.price = price;

        var object = new JSONObject();
        object.put("inventoryItemId", inventoryItemId);
        object.put("quantity", quantity);
        object.put("price", price);

        requestContext.performRequest(
                post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                .content(object.toString())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @When("I create a sale item for product code {string}, quantity {int}, price {double}, more info {string}, closing {string}")
    public void i_create_a_sale_item_for_product_code_quantity_price_more_info_closing(String productCode, int quantity, double price, String moreInfo, String closing) throws Exception {
        Product product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), productCode).orElseThrow();
        InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product).stream().findFirst().orElseThrow();
        this.inventoryItemId = inventoryItem.getId();
        this.quantity = quantity;
        this.price = price;
        this.moreInfo = moreInfo;
        this.closing = closing;

        var object = new JSONObject();
        object.put("inventoryItemId", inventoryItemId);
        object.put("quantity", quantity);
        object.put("price", price);
        object.put("moreInfo", moreInfo);
        object.put("closes", closing);

        requestContext.performRequest(
                post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                .content(object.toString())
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    @Then("I expect the sale item to be created")
    public void i_expect_the_sale_item_to_be_created() throws Exception {
        var mvcResult = requestContext.getLastResult();

        assertEquals(201, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());

        assertTrue(response.get("listingId") instanceof Number);
        Number saleItemId = (Number) response.get("listingId");

        SaleItem saleItem = saleItemRepository.findById(saleItemId.longValue()).orElseThrow();
        assertEquals(quantity.intValue(), saleItem.getQuantity());
        assertEquals(price, saleItem.getPrice().doubleValue(), 0.00001);
        assertEquals(moreInfo, saleItem.getMoreInfo());
        if (closing != null) {
            assertEquals(closing, saleItem.getCloses().toString());
        } else {
            assertEquals(saleItem.getInventoryItem().getExpires(), saleItem.getCloses());
        }
    }

    @Then("I expect the sale item not to be created, due to being forbidden")
    public void i_expect_the_sale_item_not_to_be_created_forbidden() {
        var mvcResult = requestContext.getLastResult();
        assertEquals(403, mvcResult.getResponse().getStatus());
        assertEquals(0, saleItemRepository.count());
    }

    @Then("I expect the sale item not to be created, due to being a bad request")
    public void i_expect_the_sale_item_not_to_be_created_bad_request() {
        var mvcResult = requestContext.getLastResult();
        assertEquals(400, mvcResult.getResponse().getStatus());
        assertEquals(0, saleItemRepository.count());
    }

    @When("I look a the business sale listings")
    public void i_look_a_the_business_sale_listings() throws Exception {
        requestContext.performRequest(get(String.format("/businesses/%d/listings", businessContext.getLast().getId())));
    }

    @Then("I expect to be see the sales listings")
    public void i_expect_to_be_see_the_sales_listings() throws Exception {
        var mvcResult = requestContext.getLastResult();

        assertEquals(200, mvcResult.getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");

        for (Object obj : results) {
            assertTrue(obj instanceof JSONObject);
            JSONObject json = (JSONObject)obj;

            long id = ((Number)json.get("id")).longValue();

            Optional<SaleItem> foundItem = addedSaleItems.stream().filter(x -> x.getId().equals(id)).findFirst();
            assertTrue(foundItem.isPresent());

            assertEquals(foundItem.get().getQuantity(), json.get("quantity"));
            assertEquals(foundItem.get().getPrice().doubleValue(), json.get("price"));
            assertEquals(foundItem.get().getCloses().toString(), json.get("closes"));
        }

        assertEquals(addedSaleItems.size(), response.getAsNumber("count").intValue());
    }

    @When("I like the sale item")
    public void i_like_the_sale_item() {
        assertEquals(1, addedSaleItems.size());
        SaleItem saleItem = addedSaleItems.stream().findFirst().orElseThrow();

        var json = new JSONObject();
        json.put("userId", userContext.getLast().getUserID());
        json.put("interested", true);

        requestContext.performRequest(
            put(String.format("/listings/%d/interest", saleItem.getId()))
                .content(json.toString())
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    @When("I unlike the sale item")
    public void i_unlike_the_sale_item() {
        assertEquals(1, addedSaleItems.size());
        SaleItem saleItem = addedSaleItems.stream().findFirst().orElseThrow();

        var json = new JSONObject();
        json.put("userId", userContext.getLast().getUserID());
        json.put("interested", false);

        requestContext.performRequest(
                put(String.format("/listings/%d/interest", saleItem.getId()))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    @Transactional
    @Then("The like count of the sale item is {int}")
    public void the_like_count_of_the_sale_item_is(int count) {
        assertEquals(1, addedSaleItems.size());
        SaleItem saleItem = addedSaleItems.stream().findFirst().orElseThrow();

        saleItem = saleItemRepository.findById(saleItem.getId()).orElseThrow();

        assertEquals(count, saleItem.getLikeCount());
    }

    @SneakyThrows
    private void notificationExistsWithInterest(boolean interested) {
        assertEquals(1, addedSaleItems.size());
        SaleItem saleItem = addedSaleItems.stream().findFirst().orElseThrow();

        List<InterestEventDTO> eventList = objectMapper.readValue(requestContext.getLastResultAsString(), new TypeReference<>() {});
        var event = eventList.get(0);

        assertEquals((long)saleItem.getId(), (long)event.getSaleItem().getId());
        assertEquals(event.isInterested(), interested);

    }

    @Then("The notification is for liking the sale item")
    public void the_notification_is_for_liking_the_sale_item() {
        notificationExistsWithInterest(true);
    }

    @Then("The notification is for unliking the sale item")
    public void the_notification_is_for_unliking_the_sale_item() {
        notificationExistsWithInterest(false);
    }
}
