package cucumber.stepDefinitions;

import cucumber.BusinessContext;
import cucumber.RequestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.entities.SaleItem;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class SaleItemStepDefinition {
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private RequestContext requestContext;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;

    private Integer quantity;
    private Long inventoryItemId;
    private Double price;
    private MvcResult mvcResult;
    private String closing;
    private String moreInfo;

    @When("I create a sale item for product code {string}, quantity {int}, price {double}")
    public void i_create_a_sale_item_for_product_code_quantity_price(String productCode, int quantity, double price) throws Exception {
        Product product = productRepository.findByProductCode(productCode).orElseThrow();
        InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product).stream().findFirst().orElseThrow();
        this.inventoryItemId = inventoryItem.getId();
        this.quantity = quantity;
        this.price = price;

        var object = new JSONObject();
        object.put("inventoryItemId", inventoryItemId);
        object.put("quantity", quantity);
        object.put("price", price);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                ).content(object.toString())
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create a sale item for product code {string}, quantity {int}, price {double}, more info {string}, closing {string}")
    public void i_create_a_sale_item_for_product_code_quantity_price_more_info_closing(String productCode, int quantity, double price, String moreInfo, String closing) throws Exception {
        Product product = productRepository.findByProductCode(productCode).orElseThrow();
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

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                ).content(object.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("I expect the sale item to be created")
    public void i_expect_the_sale_item_to_be_created() throws Exception {
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

    @Then("I expect the sale item not to be created")
    public void i_expect_the_sale_item_not_to_be_created() {
        assertEquals(403, mvcResult.getResponse().getStatus());
        assertEquals(0, saleItemRepository.count());
    }
}
