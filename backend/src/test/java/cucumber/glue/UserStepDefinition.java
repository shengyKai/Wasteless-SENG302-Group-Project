package cucumber.glue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.PasswordAuthenticator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class UserStepDefinition {

    @Mock
    private UserRepository userRepository;

    private User theUser;
    private String userFirstName;
    private String userMiddleName;
    private String userLastName;
    private String userNickname;
    private String userEmail;
    private String userPassword;
    private String userBio;
    private String userDob;
    private String userPhNum;
    private Location userAddress;

    @After
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @Given("the user with the email {string} does not exist")
    public void theUserWithTheEmailDoesNotExist(String email) {
        Assert.assertEquals(userRepository.findByEmail(email), null);
    }

    @When("a user is getting created the first name is {string}")
    public void aUserIsGettingCreatedTheFirstNameIs(String firstName) { userFirstName = firstName; }

    @And("their middle name is {string}")
    public void theirMiddleNameIs(String middleName) { userMiddleName = middleName; }

    @And("their last name is {string}")
    public void theirLastNameIs(String lastName) { userLastName = lastName; }

    @And("their nickname is {string}")
    public void theirNicknameIs(String nickname) { userNickname = nickname; }

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
        Assert.assertEquals(user.getDob(), dob);
    }

    @Then("the user has the phone number {string}")
    public void theUserHasThePhNum(String phNum) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getPhNum(), phNum);
    }

    @Then("the user has the address {string}")
    public void theUserHasTheAddress(String address) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertSame(user.getAddress(), Location.covertAddressStringToLocation(address));
    }
}
