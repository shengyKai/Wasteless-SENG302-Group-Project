package org.seng302.Controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.Entities.Account;
import org.seng302.Entities.Location;
import org.seng302.Persistence.AccountRepository;
import org.seng302.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() throws ParseException {
        User john = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        Account sameEmailAccount = accountRepository.findByEmail("johnsmith99@gmail.com");
        if (sameEmailAccount != null) {
            accountRepository.delete(sameEmailAccount);
        }
        accountRepository.save(john);
    }

    /**
     * Verify that when a login request is recieved with a valid username and password the user will be logged in.
     * Also checks an authentication token of length 32 is returned in the response body
     * @throws Exception
     */
    @Test
    public void loginWithValidUsernameAndPasswordTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"1337-H%nt3r2\"}";

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String authToken = JsonPath.read(result.getResponse().getContentAsString(), "$.authToken");
        assertTrue(authToken.length() == 32);
        // Might change to cookies within the future
    }

    /**
     * Verify that when a login request is recieved with an email which is not stored in the account table the user
     * will not be logged in and status code 400 will be returned.
     * @throws Exception
     */
    @Test
    public void loginWithIncorrectPasswordTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"Wrong password\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Password is incorrect"));
    }

    /**
     * Verify that when a login request is recieved with an email which is stored in the account table and an incorrect
     * password for that email the user will not be logged in and status code 400 will be returned.
     * @throws Exception
     */
    @Test
    public void loginWithIncorrectEmailTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith100@gmail.com\", \"password\": \"1337-H%nt3r2\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("There is no account associated with this email"));
    }
}