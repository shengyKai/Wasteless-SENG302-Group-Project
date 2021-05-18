package cucumber.stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.seng302.entities.Product;
import org.seng302.entities.User;
import org.seng302.persistence.AccountRepository;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ProductStepDefinition {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private Product product;
    private User owner;
    private Business business;
    private Business secondBusiness;

    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userRepository.save(owner);
        business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(business);
    }

    @When("the product code {string} and the name {string} is provided")
    public void theProductCodeAndTheNameIsProvided(String productCode, String name) {
        try {
            product = new Product.Builder()
                    .withProductCode(productCode)
                    .withName(name)
                    .withBusiness(business)
                    .build();
        } catch (ResponseStatusException | NullPointerException ignored) {}
    }

    @Then("the product {string} exists for the business")
    public void theProductExists(String prodCode) {
        productRepository.save(product);
        Assertions.assertNotNull(productRepository.findByBusinessAndProductCode(business, prodCode));
    }

    @And("the time of {string} created is set to now")
    public void timeSetNow(String prodCode) {
        product = productRepository.findByBusinessAndProductCode(business, prodCode).get();
        Instant created = product.getCreated();
        assert(ChronoUnit.SECONDS.between(Instant.now(), created) < 20);
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
        Assertions.assertTrue(productRepository.findByBusinessAndProductCode(business, prodCode).isEmpty());
    }

    @Given("the business does not exist")
    public void theBusinessDoesNotExist() {
        business = null;
    }

    @And("a business has a product {string} with name {string}")
    public void hasAProduct(String prodCode, String prodName) {
        product = new Product.Builder()
                .withProductCode(prodCode)
                .withName(prodName)
                .withBusiness(business)
                .build();
        productRepository.save(product);
    }

    @And("a second business {string} exists")
    public void aSecondBusinessExists(String name) throws ParseException {
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing2")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userRepository.save(owner);
        secondBusiness = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withBusinessType("Retail Trade")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(secondBusiness);
    }

    @When("the second business creates a product {string}")
    public void theSecondBusinessCreatesAProduct(String prodCode) {
        product = new Product.Builder()
                .withProductCode(prodCode)
                .withName("New Product")
                .withBusiness(secondBusiness)
                .build();
    }

    @And("the product {string} exists for the second business")
    public void theProductExistsForTheSecondBusiness(String prodCode) {
        productRepository.save(product);
        Assertions.assertNotNull(productRepository.findByBusinessAndProductCode(secondBusiness, prodCode));
    }

    @Then("only the first product {string} exists, not with name {string}")
    public void onlyTheFirstProductExists(String prodCode, String desc) {
        product = productRepository.findByBusinessAndProductCode(business, prodCode).get();
        Assertions.assertNotEquals(desc, product.getDescription());
    }
}
