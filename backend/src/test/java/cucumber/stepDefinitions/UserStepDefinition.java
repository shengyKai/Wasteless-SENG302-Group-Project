package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.After;
import org.junit.Assert;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserStepDefinition {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private User theUser;
    private String userFirstName = "Bob";
    private String userMiddleName = "Bob";
    private String userLastName = "Bob";
    private String userNickname = "Bob";
    private String userEmail = "Bob@bob.com";
    private String userPassword = "B0bbbbbbbbbbbb";
    private String userBio = "I am Bob";
    private String userDob = "10-10-1999";
    private String userPhNum = "0270000000";
    private Location userAddress = Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,1010");

    private Long userID;

    @After
    public void Setup() {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        userRepository.deleteAll();
    }

    @Given("the user with the email {string} does not exist")
    public void theUserWithTheEmailDoesNotExist(String email) {
        Assert.assertNull(userRepository.findByEmail(email));
    }

    @When("a user is getting created the first name is {string}")
    public void aUserIsGettingCreatedTheFirstNameIs(String firstName) { userFirstName = firstName; }

    @And("their middle name is {string}")
    public void theirMiddleNameIs(String middleName) { userMiddleName = middleName; }

    @And("their last name is {string}")
    public void theirLastNameIs(String lastName) { userLastName = lastName; }

    @And("their nickname is {string}")
    public void theirNicknameIs(String nickname) { userNickname = nickname; }

    @Given("a user with the email {string} is created")
    @And("their email is {string}")
    public void theirEmailIs(String email) { userEmail = email; }

    @And("their password is {string}")
    public void theirPasswordIs(String password) { userPassword = password; }

    @And("their bio is {string}")
    public void theirBioIs(String bio) { userBio = bio; }

    @And("their date of birth is {string}")
    public void theirDobIs(String dob) { userDob = dob; }

    @And("their phone number is {string}")
    public void theirPhNumIs(String phNum) { userPhNum = phNum; }

    @And("their address is {string}")
    public void theirAddressIs(String address) { userAddress = Location.covertAddressStringToLocation(address); }

    @And("the user is created")
    public void theUserIsCreated() throws ParseException {
        theUser = new User.Builder()
                .withFirstName(userFirstName)
                .withMiddleName(userMiddleName)
                .withLastName(userLastName)
                .withNickName(userNickname)
                .withEmail(userEmail)
                .withPassword(userPassword)
                .withBio(userBio)
                .withDob(userDob)
                .withPhoneNumber(userPhNum)
                .withAddress(userAddress)
                .build();
        userRepository.save(theUser);
    }

    @Then("a user with the email {string} exists")
    public void aUserWithTheEmailExists(String email) { Assert.assertNotNull(userRepository.findByEmail(email)); }

    @Then("the user has the first name {string}")
    public void theUserHasTheFirstName(String firstName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getFirstName(), firstName);
    }

    @Then("the user has the middle name {string}")
    public void theUserHasTheMiddleName(String middleName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getMiddleName(), middleName);
    }

    @Then("the user has the last name {string}")
    public void theUserHasTheLastName(String lastName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getLastName(), lastName);
    }

    @Then("the user has the nickname {string}")
    public void theUserHasTheNickname(String nickname) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getNickname(), nickname);
    }

    @Then("the user has the email {string}")
    public void theUserHasTheEmail(String email) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getEmail(), email);
    }

    @Then("the user has the password {string}")
    public void theUserHasThePassword(String password) throws NoSuchAlgorithmException {
        User user = userRepository.findByEmail(userEmail);
        String authenticationCode = PasswordAuthenticator.generateAuthenticationCode(password);
        Assert.assertEquals(user.getAuthenticationCode(), authenticationCode);
    }

    @Then("the user has the bio {string}")
    public void theUserHasTheBio(String bio) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getBio(), bio);
    }

    @Then("the user has the date of birth {string}")
    public void theUserHasTheDob(String dob) {
        User user = userRepository.findByEmail(userEmail);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Assert.assertEquals(user.getDob().getTime(), dateFormat.parse(dob).getTime());
        } catch (ParseException parseException) {
            Assert.fail();
        }
    }

    @Then("the user has the phone number {string}")
    public void theUserHasThePhNum(String phNum) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getPhNum(), phNum);
    }

    @Then("the user has the address {string}")
    public void theUserHasTheAddress(String address) {
        User user = userRepository.findByEmail(userEmail);
        Location location = Location.covertAddressStringToLocation(address);
        location.setId(user.getAddress().getId());
        Assert.assertEquals(user.getAddress(), location);
    }

    @Given("the user possesses the email {string}")
    public void theUserPossessesTheEmail(String email) { userEmail = email; }

    @And("the user possesses the password {string}")
    public void theUserPossessesThePassword(String password) { userPassword = password; }

    @When("the user logs in")
    public void theUserLogsInWithTheEmail() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("password", userPassword);
        requestBody.put("email", userEmail);

        try {
            MvcResult result = (MvcResult) mockMvc.perform(post("/login")
                    .content(objectMapper.writeValueAsString(requestBody.toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            userID = (Long) jsonObject.getAsNumber("userId");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Then("the user is logged in")
    public void theUserWithTheEmailIsLoggedIn() {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getUserID(), userID);
    }

}
