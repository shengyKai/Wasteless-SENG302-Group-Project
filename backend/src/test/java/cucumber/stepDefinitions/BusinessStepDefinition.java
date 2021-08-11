package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.BusinessContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Assert;
import org.seng302.leftovers.dto.ModifyBusinessDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class BusinessStepDefinition {
    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserContext userContext;
    @Autowired
    private RequestContext requestContext;

    private JSONObject modifyParameters;

    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {

        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
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
                .withBusinessType(type)
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

    private void placeValueAtPath(Map<String, Object> mapping, List<String> path, Object value) {
        if (path.size() == 1) {
            mapping.put(path.get(0), value);
        } else {
            String head = path.get(0);
            if (!mapping.containsKey(head)) {
                mapping.put(head, new HashMap<>());
            }
            placeValueAtPath((Map<String, Object>)mapping.get(head), path.subList(1, path.size()), value);
        }
    }

    @When("I try to updated the fields of the business to:")
    public void i_try_to_updated_the_fields_of_the_business_to(Map<String, Object> dataTable) {
        modifyParameters = new JSONObject();
        for (var entry : dataTable.entrySet()) {
            List<String> path = Arrays.asList(entry.getKey().split("\\."));
            placeValueAtPath(modifyParameters, path, entry.getValue());
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
        assertEquals(modifyParameters.get("businessType"), business.getBusinessType());
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
}
