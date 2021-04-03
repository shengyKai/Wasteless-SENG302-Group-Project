package org.seng302.Controllers;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
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


    /**
     * Add a user object to the userRepository and construct an authorization token to be used for this session.
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
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("COSC co")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness);
    }
    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestUser() throws ParseException {
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress("4 Rountree Street, Upper Riccarton")
                .build();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(owner);
    }

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
        assertEquals(address.getAsString("postcode"), object.getAddress().getZipCode());
    }


    @Test
    public void RegisterBusinessTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Business newBusiness = businessRepository.findByName("Lumbridge General Store");
        assertEquivalentJsonToObject(businessJson, newBusiness);
    }


}
