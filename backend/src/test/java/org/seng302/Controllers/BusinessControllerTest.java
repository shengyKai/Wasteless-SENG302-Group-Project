package org.seng302.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BusinessControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private Business testBusiness;
    private User owner;
    private User admin;
    private User otherUser;
    /**
     * Add a user object to the userRepository and construct an authorization token
     * to be used for this session.
     *
     * @throws ParseException
     */
    @BeforeEach
    public void setUp() throws ParseException, IOException {
        setUpAuthCode();
        setUpTestUser();
        setUpTestBusiness();
    }

    private void setCurrentUser(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode() {
        StringBuilder authCodeBuilder = new StringBuilder();
        authCodeBuilder.append("0".repeat(64));
        String authCode = authCodeBuilder.toString();
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }

    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestBusiness() throws ParseException {
        testBusiness = new Business.Builder().withBusinessType("Accommodation and Food Services")
                .withAddress(new Location()).withDescription("Some description").withName("COSC co")
                .withPrimaryOwner(owner).build();
        businessRepository.save(testBusiness);
        testBusiness.addAdmin(admin);
        businessRepository.save(testBusiness);
    }

    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestUser() throws ParseException {
        Location userAddress = new Location.Builder().inCountry("New Zealand").inRegion("Canterbury")
                .inCity("Christchurch").inSuburb("Ilam").atStreetNumber("123").onStreet("Ilam road")
                .withPostCode("8041").build();
        owner = new User.Builder().withFirstName("John").withMiddleName("Hector").withLastName("Smith")
                .withNickName("Jonny").withEmail("johnsmith99@gmail.com").withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach").withDob("2001-03-11").withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location()).build();
        admin = new User.Builder().withFirstName("Caroline").withMiddleName("Jane").withLastName("Smith")
                .withNickName("Carrie").withEmail("carriesmith@hotmail.com").withPassword("h375dj82")
                .withDob("2001-03-11").withPhoneNumber("+64 3 748 7562").withAddress(new Location()).build();
        otherUser = new User.Builder().withFirstName("William").withLastName("Pomeroy").withNickName("Will")
                .withEmail("pomeroy.will@outlook.com").withPassword("569277hghrud").withDob("1981-03-11")
                .withPhoneNumber("+64 21 099 5786").withAddress(userAddress).build();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(owner);
        userRepository.save(admin);
        userRepository.save(otherUser);
    }

    /**
     * AssertEquals each property of a Business as type JSON to type Object If the
     * two objects are equal, no error is thrown
     * 
     * @param json   The JSON representation of a business
     * @param object The Object representation of a business
     */
    private void assertEquivalentJsonToObject(JSONObject json, Business object) {
        assertEquals(json.getAsString("primaryAdministratorId"), object.getPrimaryOwner().getUserID().toString());
        assertEquals(json.getAsString("name"), object.getName());
        assertEquals(json.getAsString("description"), object.getDescription());
        assertEquals(json.getAsString("businessType"), object.getBusinessType());
        JSONObject address = new JSONObject((Map<String, ?>) json.get("address"));
        assertEquals(address.getAsString("streetNumber"), object.getAddress().getStreetNumber().toString());
        assertEquals(address.getAsString("streetName"), object.getAddress().getStreetName());
        assertEquals(address.getAsString("city"), object.getAddress().getCity());
        assertEquals(address.getAsString("region"), object.getAddress().getRegion());
        assertEquals(address.getAsString("country"), object.getAddress().getCountry());
        assertEquals(address.getAsString("postcode"), object.getAddress().getPostCode());
    }

    /**
     * Test for registering a business in a blue sky scenario Session logged in as
     * the given primaryAdministratorId All request values are valid
     * 
     * @throws Exception
     */
    @Test
    public void RegisterBusinessTest() throws Exception {
        String businessJsonString = String.format("{\n" + "  \"primaryAdministratorId\": %s,\n"
                + "  \"name\": \"Lumbridge General Store\",\n"
                + "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" + "  \"address\": {\n"
                + "    \"streetNumber\": \"324\",\n" + "    \"streetName\": \"Ilam Road\",\n"
                + "    \"city\": \"Christchurch\",\n" + "    \"region\": \"Canterbury\",\n"
                + "    \"country\": \"New Zealand\",\n" + "    \"postcode\": \"90210\"\n" + "  },\n"
                + "  \"businessType\": \"Accommodation and Food Services\"\n" + "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken).cookie(authCookie).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        Business newBusiness = businessRepository.findByName("Lumbridge General Store");
        assertEquivalentJsonToObject(businessJson, newBusiness);
    }

    /**
     * Test for registering a business when the business type is not one of the
     * expected types Session logged in as the given primaryAdministratorId Business
     * type value is invalid
     * 
     * @throws Exception
     */
    @Test
    public void RegisterBusinessInvalidBusinessTypeTest() throws Exception {
        String businessJsonString = String.format("{\n" + "  \"primaryAdministratorId\": %s,\n"
                + "  \"name\": \"Lumbridge General Store\",\n"
                + "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" + "  \"address\": {\n"
                + "    \"streetNumber\": \"324\",\n" + "    \"streetName\": \"Ilam Road\",\n"
                + "    \"city\": \"Christchurch\",\n" + "    \"region\": \"Canterbury\",\n"
                + "    \"country\": \"New Zealand\",\n" + "    \"postcode\": \"90210\"\n" + "  },\n"
                + "  \"businessType\": \"An invalid BUSINESS TYPE\"\n" + "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken).cookie(authCookie).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    /**
     * Test for registering a business when not logged in as a user Session not
     * logged in All request values are valid
     * 
     * @throws Exception
     */
    @Test
    public void RegisterBusinessWhenNotLoggedIn() throws Exception {
        String businessJsonString = String.format("{\n" + "  \"primaryAdministratorId\": %s,\n"
                + "  \"name\": \"Lumbridge General Store\",\n"
                + "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" + "  \"address\": {\n"
                + "    \"streetNumber\": \"324\",\n" + "    \"streetName\": \"Ilam Road\",\n"
                + "    \"city\": \"Christchurch\",\n" + "    \"region\": \"Canterbury\",\n"
                + "    \"country\": \"New Zealand\",\n" + "    \"postcode\": \"90210\"\n" + "  },\n"
                + "  \"businessType\": \"Accommodation and Food Services\"\n" + "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test for registering a business when the given primaryAdministrator doesn't
     * exist Session logged in as the given primaryAdministratorId
     * primaryAdministrator is invalid
     * 
     * @throws Exception
     */
    @Test
    public void RegisterBusinessInvalidIdTest() throws Exception {
        String businessJsonString = "{\n" + "  \"primaryAdministratorId\": 999,\n"
                + "  \"name\": \"Lumbridge General Store\",\n"
                + "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" + "  \"address\": {\n"
                + "    \"streetNumber\": \"324\",\n" + "    \"streetName\": \"Ilam Road\",\n"
                + "    \"city\": \"Christchurch\",\n" + "    \"region\": \"Canterbury\",\n"
                + "    \"country\": \"New Zealand\",\n" + "    \"postcode\": \"90210\"\n" + "  },\n"
                + "  \"businessType\": \"Accommodation and Food Services\"\n" + "}";
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken).cookie(authCookie).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    /**
     * Test for registering a business under someone else's name Session logged in
     * as a different user than the provided primaryAdministratorId
     * primaryAdministrator is invalid
     * 
     * @throws Exception
     */
    @Test
    public void RegisterBusinessNoPermissionTest() throws Exception {
        String businessJsonString = String.format("{\n" + "  \"primaryAdministratorId\": %s,\n"
                + "  \"name\": \"Lumbridge General Store\",\n"
                + "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" + "  \"address\": {\n"
                + "    \"streetNumber\": \"324\",\n" + "    \"streetName\": \"Ilam Road\",\n"
                + "    \"city\": \"Christchurch\",\n" + "    \"region\": \"Canterbury\",\n"
                + "    \"country\": \"New Zealand\",\n" + "    \"postcode\": \"90210\"\n" + "  },\n"
                + "  \"businessType\": \"Accommodation and Food Services\"\n" + "}", owner.getUserID());
        setCurrentUser(999L);
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken).cookie(authCookie).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is not logged in, the response has a 401 status code and an empty body.
     */
    @Test
    public void getBusinessByIdUnauthorizedTest() throws Exception {
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId() + 1)))
        .andExpect(status().isUnauthorized()).andReturn();
        System.out.println(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in, but the id given in the request URL does not correspond to a
     * business in the database, the response has a 406 status code and an empty
     * body.
     */
    @Test
    public void getBusinessByIdDoesNotExistTest() throws Exception{
        setCurrentUser(owner.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId() + 1))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isNotAcceptable()).andReturn();
        System.out.println(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in as the owner of the business with the given id, the response has
     * a 200 status code and the body contains a JSON representation of the business
     * with the given id.
     * 
     * @throws Exception
     */
    @Test
    public void getBusinessLoggedInAsOwnerTest() throws Exception {
        testBusiness = businessRepository.findById(testBusiness.getId()).get();
        setCurrentUser(owner.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertEquals(owner.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertTrue(adminString.contains(String.format("\"id\":%d", owner.getUserID())));
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(testBusiness.constructJson(true).toJSONString()), mapper.readTree(json.toJSONString()));
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in as an admin of the business with the given id, the response has
     * a 200 status code and the body contains a JSON representation of the business
     * with the given id.
     */
    @Test
    public void getBusinessLoggedInAsAdminTest() throws Exception {
        testBusiness = businessRepository.findById(testBusiness.getId()).get();
        setCurrentUser(admin.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertNotEquals(admin.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertTrue(adminString.contains(String.format("\"id\":%d", admin.getUserID())));
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(testBusiness.constructJson(true).toJSONString()), mapper.readTree(json.toJSONString()));
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in to an account which is not an owner or admin of the business
     * with the given id, the response has a 200 status code and the body contains a
     * JSON representation of the business with the given id.
     */
    @Test
    public void getBusinessLoggedInAsOtherTest() throws Exception {
        testBusiness = businessRepository.findById(testBusiness.getId()).get();
        setCurrentUser(otherUser.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertNotEquals(otherUser.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertFalse(adminString.contains(String.format("\"id\":%d", otherUser.getUserID())));
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(testBusiness.constructJson(true).toJSONString()), mapper.readTree(json.toJSONString()));
    }

}
