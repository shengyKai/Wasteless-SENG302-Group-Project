package cucumber.stepDefinitions;

import cucumber.BusinessContext;
import cucumber.RequestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

    private Integer quantity;
    private Long inventoryItemId;
    private Double price;
    private MvcResult mvcResult;

    @When("I create a sale item for product code {string}, quantity {int}, price {double}")
    public void i_create_a_sale_item_for_product_code_quantity_price(String productCode, int quantity, double price) throws Exception {
        Product product = productRepository.findByProductCode(productCode).orElseThrow();
        InventoryItem inventoryItem = inventoryItemRepository.findAllByProduct(product).stream().findFirst().orElseThrow();
        this.inventoryItemId = inventoryItem.getId();
        this.quantity = quantity;
        this.price = price;
        String postBody = String.format(
                "{ " +
                    "\"inventoryItemId\": \"%s\"," +
                    "\"quantity\": %d," +
                    "\"price\": \"%s\"" +
                "}"
                , inventoryItemId, quantity, price);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/listings", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("I expect the sale item to be created")
    public void i_expect_the_sale_item_to_be_created() {

    }
}
