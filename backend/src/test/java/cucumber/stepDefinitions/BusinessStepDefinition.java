package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.BusinessContext;
import cucumber.context.ImageContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import cucumber.utils.CucumberUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.seng302.datagenerator.ExampleDataFileReader;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class BusinessStepDefinition {
    @Value("${storage-directory}")
    private Path root;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ImageContext imageContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private SessionFactory sessionFactory;


    private JSONObject modifyParameters;

    /**
     * Method to save multiple products into the latest business saved in the businessContext
     * @param amount Number of products to generate to save into the business
     */
    @Transactional
    public void saveMultipleProductsToBusiness(int amount) {
        var business = businessContext.getLast();
        for (int i = 0; i < amount; i++) {
            var product = new Product.Builder()
                    .withBusiness(business)
                    .withDescription("some description")
                    .withManufacturer("Some manufacturer")
                    .withName("Some prod")
                    .withProductCode("PROD" + String.valueOf(i))
                    .withRecommendedRetailPrice("123")
                    .build();
            business.addToCatalogue(product);
        }
        businessContext.save(business);
    }


    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {

        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType(BusinessType.RETAIL_TRADE)
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

    @Given("the business {string} with the type {string} exists")
    public void the_business_with_the_type_exists(String name, String type) {
        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType(objectMapper.convertValue(type, BusinessType.class))
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(userContext.getLast())
                .build();
        businessContext.save(business);
    }

    @When("I search for business type {string}")
    public void i_search_for_business_type(String businessType) {
        requestContext.performRequest(get("/businesses/search")
                .param("businessType", businessType));
    }

    @When("I search with query {string} and business type {string}")
    public void i_search_with_query_and_business_type(String query, String businessType) {
        requestContext.performRequest(get("/businesses/search")
                .param("searchQuery", query)
                .param("businessType", businessType));
    }



    @When("I try to updated the fields of the business to:")
    public void i_try_to_updated_the_fields_of_the_business_to(Map<String, Object> dataTable) {
        modifyParameters = new JSONObject();
        for (var entry : dataTable.entrySet()) {
            List<String> path = Arrays.asList(entry.getKey().split("\\."));
            CucumberUtils.setValueAtPath(modifyParameters, path, entry.getValue());
        }

        String adminName = (String)modifyParameters.remove("primaryAdministrator");
        if (adminName != null) {
            modifyParameters.put("primaryAdministratorId", userContext.getByName(adminName).getUserID());
        }

        Business business = businessContext.getLast();
        requestContext.performRequest(put("/businesses/" + business.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.modifyParameters.toJSONString()));
    }

    @Then("The business is updated")
    public void the_business_is_updated() {
        Business business = businessRepository.getBusinessById(businessContext.getLast().getId());
        assertEquals(modifyParameters.get("name"), business.getName());
        assertEquals(modifyParameters.get("description"), business.getDescription());
        assertEquals(modifyParameters.get("businessType"), objectMapper.convertValue(business.getBusinessType(), String.class));
        assertEquals(modifyParameters.get("primaryAdministratorId"), business.getPrimaryOwner().getUserID());

        Map<String, Object> addressParams = (Map<String, Object>)modifyParameters.get("address");
        Location address = business.getAddress();
        assertEquals(addressParams.get("streetNumber"), address.getStreetNumber());
        assertEquals(addressParams.get("streetName"), address.getStreetName());
        assertEquals(addressParams.get("district"), address.getDistrict());
        assertEquals(addressParams.get("city"), address.getCity());
        assertEquals(addressParams.get("region"), address.getRegion());
        assertEquals(addressParams.get("country"), address.getCountry());
        assertEquals(addressParams.get("postcode"), address.getPostCode());
    }

    @Then("The business is not updated")
    public void the_business_is_not_updated() {
        Business business = businessContext.getLast();
        Business updatedBusiness = businessRepository.getBusinessById(business.getId());

        Set<Long> expectedOwnerAndAdminIds = business.getOwnerAndAdministrators().stream().map(User::getUserID).collect(Collectors.toSet());
        Set<Long> actualOwnerAndAdminIds   = updatedBusiness.getOwnerAndAdministrators().stream().map(User::getUserID).collect(Collectors.toSet());

        assertEquals(business.getName(),         updatedBusiness.getName());
        assertEquals(business.getDescription(),  updatedBusiness.getDescription());
        assertEquals(business.getBusinessType(), updatedBusiness.getBusinessType());
        assertEquals(expectedOwnerAndAdminIds,   actualOwnerAndAdminIds);
        assertEquals(business.getAddress(),      updatedBusiness.getAddress());
    }

    @Given("There are products related to the business")
    public void there_are_products_related_to_the_business() {
        Business business = businessRepository.getBusinessById(businessContext.getLast().getId());
        saveMultipleProductsToBusiness(5);
    }

    @Then("The all of the business product's country of sale is updated")
    public void the_all_of_the_business_product_s_country_of_sale_is_updated() {
        Map<String, Object> addressParams = (Map<String, Object>)modifyParameters.get("address");

        try (Session session = sessionFactory.openSession()) {
            var business = session.find(Business.class, businessContext.getLast().getId());
            List<Product> catalogue = business.getCatalogue();
            for (Product product : catalogue) {
                assertEquals(product.getCountryOfSale(), addressParams.get("country"));
            }
        }
    }

    @When("I try to upload the image {string} to the business")
    public void i_try_to_upload_the_image_to_the_business(String filename) throws IOException {
        String contentType;
        if (filename.endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.endsWith(".jpg")) {
            contentType = "image/jpeg";
        } else if (filename.endsWith(".txt")) {
            contentType = "text/plain";
        } else {
            fail("Could not parse content type for: \"" + filename + "\"");
            return;
        }

        InputStream stream = ExampleDataFileReader.class.getResourceAsStream("/" + filename);
        requestContext.performRequest(multipart("/businesses/" + businessContext.getLast().getId() + "/images")
                .file(new MockMultipartFile("file", filename, contentType, stream)));
    }

    @Transactional
    @Then("The business has no images")
    public void the_business_has_no_images() {
        Business business = businessRepository.getBusinessById(businessContext.getLast().getId());
        assertEquals(0, business.getImages().size());
    }

    @Transactional
    @Then("The business has one image")
    public void the_business_has_one_image() {
        Business business = businessRepository.getBusinessById(businessContext.getLast().getId());
        assertEquals(1, business.getImages().size());
        Image image = business.getImages().get(0);

        assertNotNull(image.getFilename());
        Path imagePath = root.resolve(image.getFilename());
        assertTrue(imagePath.toFile().exists());

        assertNotNull(image.getFilenameThumbnail());
        Path thumbnailPath = root.resolve(image.getFilenameThumbnail());
        assertTrue(thumbnailPath.toFile().exists());
    }

    @Given("The business {string} has primary image {string}")
    public void the_business_has_primary_image_with_name(String businessName, String imageName) {
        Business business = businessContext.getByName(businessName);
        Image image = new Image(imageName, imageName + "_thumbnail.png");
        image = imageContext.save(image);
        business.addImage(0, image);
        business = businessContext.save(business);

        assertFalse(business.getImages().isEmpty());
        assertEquals(image, business.getImages().get(0));

    }

    @Given("The business {string} has image {string}")
    public void the_business_has_image_with_name(String businessName, String imageName) {
        Business business = businessContext.getByName(businessName);
        Image image = new Image(imageName, imageName + "_thumbnail.png");
        image = imageContext.save(image);
        business.addImage(image);
        business = businessContext.save(business);

        assertFalse(business.getImages().isEmpty());
        assertTrue(business.getImages().contains(image));

    }
    @Transactional
    @When("I try to set the primary image for {string} to {string}")
    public void i_try_set_primary_image_to(String businessName, String imageName) {
        Image image = imageContext.getByName(imageName);
        Business business = businessRepository.getBusinessById(businessContext.getByName(businessName).getId());

        assertFalse(business.getImages().isEmpty());
        assertNotEquals(image, business.getImages().get(0));

        requestContext.performRequest(
                put("/businesses/"
                        + business.getId()
                        + "/images/"
                        + image.getID()
                        + "/makeprimary"));

    }

    @Transactional
    @Then("Image {string} is the primary image for {string}")
    public void image_is_the_primary_image(String imageName, String businessName) {
        Image image = imageContext.getByName(imageName);
        Business business = businessRepository.getBusinessById(businessContext.getByName(businessName).getId());

        assertFalse(business.getImages().isEmpty());
        assertEquals(image, business.getImages().get(0)); // index zero is primary image
    }

    @And("the business {string} with the type {string} and location {string} exists")
    public void theBusinessWithTheTypeAndLocationExists(String name, String type, String location) {
        ObjectMapper objectMapper = new ObjectMapper();
        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType(objectMapper.convertValue(type, new TypeReference<>() {}))
                .withAddress(Location.covertAddressStringToLocation(location))
                .withPrimaryOwner(userContext.getLast())
                .build();
        businessContext.save(business);
    }

    @When("I view the business {string}")
    public void i_view_the_business(String name) {
        Business business = businessContext.getByName(name);
        requestContext.performRequest(get("/businesses/" + business.getId()));
    }

    @Then("I am able to see the points for the business")
    public void i_can_see_business_points() throws net.minidev.json.parser.ParseException, UnsupportedEncodingException {
        var result = requestContext.getLastResult();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertTrue(response.containsKey("points"));
    }

    @Given("The business has {int} points")
    public void the_business_has_points(int points) {
        var business = businessContext.getLast();
        business.setPoints(points);
        businessContext.save(business);
    }

    @Then("I expect the business to have {int} points")
    public void i_expect_business_to_have_points(int points) {
        var business = businessRepository.getBusinessById(businessContext.getLast().getId()) ;
        assertEquals(points, business.getPoints());
    }
}
