package cucumber.stepDefinitions;

import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class ProductStepDefinition {

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private BusinessContext businessContext;

    @Autowired
    private ProductRepository productRepository;

    private Product product;
    private JSONObject productJSON;

    @When("the product code {string} and the name {string} is provided")
    public void theProductCodeAndTheNameIsProvided(String productCode, String name) {
        try {
            product = new Product.Builder()
                    .withProductCode(productCode)
                    .withName(name)
                    .withBusiness(businessContext.getLast())
                    .build();
        } catch (ResponseStatusException | NullPointerException ignored) {}
    }

    @Then("the product {string} exists for the business")
    public void theProductExists(String prodCode) {
        productRepository.save(product);
        Assertions.assertNotNull(productRepository.findByBusinessAndProductCode(businessContext.getLast(), prodCode));
    }

    @And("the time of {string} created is set to now")
    public void timeSetNow(String prodCode) {
        product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), prodCode).orElseThrow();
        Instant created = product.getCreated();
        Assertions.assertTrue(ChronoUnit.SECONDS.between(Instant.now(), created) < 20);
    }

    @And("the description {string}, manufacturer {string}, and retail price {string} is provided")
    public void theDescriptionManufacturerAndRetailPriceIsProvided(String desc, String man, String price) {
        try {
            product.setDescription(desc);
            product.setManufacturer(man);
            product.setRecommendedRetailPrice(new BigDecimal(price));
        } catch (NumberFormatException ignored){}
    }

    @And("all fields have a value")
    public void allFieldsHaveAValue() {
        Assertions.assertNotNull(product.getName());
        Assertions.assertNotNull(product.getProductCode());
        Assertions.assertNotNull(product.getBusiness());
        Assertions.assertNotNull(product.getCreated());
        Assertions.assertNotNull(product.getDescription());
        Assertions.assertNotNull(product.getManufacturer());
        Assertions.assertNotNull(product.getRecommendedRetailPrice());
    }

    @And("the other fields are null")
    public void theOtherFieldsAreNull() {
        Assertions.assertNotNull(product.getName());
        Assertions.assertNotNull(product.getProductCode());
        Assertions.assertNotNull(product.getBusiness());
        Assertions.assertNotNull(product.getCreated());
        Assertions.assertNull(product.getDescription());
        Assertions.assertNull(product.getManufacturer());
        Assertions.assertNull(product.getRecommendedRetailPrice());
    }

    @Then("the product {string} does not exist for the business")
    public void theProductDoesNotExistForTheBusiness(String prodCode) {
        Assertions.assertTrue(productRepository.findByBusinessAndProductCode(businessContext.getLast(), prodCode).isEmpty());
    }

    @And("a business has a product {string} with name {string}")
    public void hasAProduct(String prodCode, String prodName) {
        product = new Product.Builder()
                .withProductCode(prodCode)
                .withName(prodName)
                .withBusiness(businessContext.getLast())
                .build();
        product = productRepository.save(product);
    }


    @And("the product {string} exists for the business {string}")
    public void theProductExistsForTheSecondBusiness(String productCode, String businessName) {
        var business = businessContext.getByName(businessName);
        Assertions.assertTrue(productRepository.findByBusinessAndProductCode(business, productCode).isPresent());
    }

    @Then("only the first product {string} exists, not with name {string}")
    public void onlyTheFirstProductExists(String prodCode, String desc) {
        product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), prodCode).orElseThrow();
        Assertions.assertNotEquals(desc, product.getDescription());
    }

    @When("the business {string} creates a product {string}")
    public void theBusinessCreatesAProduct(String businessName, String productCode) {
        var product = new Product.Builder()
                .withProductCode(productCode)
                .withName("New Product")
                .withBusiness(businessContext.getByName(businessName))
                .build();
        productRepository.save(product);
    }

    @When("I update the fields of the {string} product to:")
    public void i_update_the_fields_of_the_product_to(String name, Map<String, String> dataTable) throws Exception {
        productJSON = new JSONObject(dataTable);
        productJSON.remove("property");

        product = productRepository.getProduct(businessContext.getLast(), name);

        requestContext.performRequest(
                        put(String.format("/businesses/%d/products/%s", businessContext.getLast().getId(), name))
                        .content(productJSON.toString())
                        .contentType(MediaType.APPLICATION_JSON));
        product = productRepository.findById(product.getID()).orElseThrow();
    }

    @Then("The product is updated")
    public void the_product_is_updated() {
        Assertions.assertEquals(200, requestContext.getLastResult().getResponse().getStatus());

        Assertions.assertEquals(productJSON.get("id"), product.getProductCode());
        Assertions.assertEquals(productJSON.get("name"), product.getName());
        Assertions.assertEquals(productJSON.get("description"), product.getDescription());
        Assertions.assertEquals(productJSON.get("manufacturer"), product.getManufacturer());

        Number expectedPrice = productJSON.getAsNumber("recommendedRetailPrice");
        if (expectedPrice == null) {
            Assertions.assertNull(product.getRecommendedRetailPrice());
        } else {
            Assertions.assertNotNull(product.getRecommendedRetailPrice());
            Assertions.assertEquals(expectedPrice.doubleValue(), product.getRecommendedRetailPrice().doubleValue(), 1e-8);
        }
    }
}
