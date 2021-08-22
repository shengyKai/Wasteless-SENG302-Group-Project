package cucumber.context;

import io.cucumber.java.Before;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private Cookie authCookie;
    private final Map<String, Object> sessionAuthToken = new HashMap<>();
    private User loggedInUser;
    private MvcResult lastResult;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {
        authCookie = null;
        sessionAuthToken.clear();
        lastResult = null;
        loggedInUser = null;
    }

    /**
     * Sets the currently logged in user
     * @param user User to login as or null to logout
     */
    public void setLoggedInAccount(User user) {
        if (user == null) {
            authCookie = null;
            sessionAuthToken.clear();
        } else {
            String authCode = "0".repeat(64);
            authCookie = new Cookie("AUTHTOKEN", authCode);
            sessionAuthToken.put("AUTHTOKEN", authCode);
            sessionAuthToken.put("accountId", user.getUserID());
            sessionAuthToken.put("role", user.getRole());
        }
        loggedInUser = user;
    }

    /**
     * Adds authentication to the provided request builder, if already set
     * @param builder Builder to add authentication to
     * @return Modified builder with authentication
     */
    public MockHttpServletRequestBuilder addAuthorisationToken(MockHttpServletRequestBuilder builder) {
        if (authCookie == null) {
            return builder; // If no authorisation then do nothing
        }

        return builder
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie);
    }

    /**
     * Adds authentication and performs a request from the provided builder
     * The result will also be saved and be accessible from "getLastRequest"
     * @param builder Request to execute
     * @return Result of the request
     */
    public MvcResult performRequest(MockHttpServletRequestBuilder builder) {
        lastResult = Assertions.assertDoesNotThrow(() -> mockMvc.perform(addAuthorisationToken(builder)).andReturn());
        return lastResult;
    }

    /**
     * Get the ID number of the user who is currently authenticated in this class's authentication token and request
     * cookie.
     * @return The ID number of the authenticated user
     */
    public Long getLoggedInId() {
        return loggedInUser.getUserID();
    }

    /**
     * Get the last result from perform request
     * @return MvcResult of the last performed request
     */
    public MvcResult getLastResult() {
        return lastResult;
    }
}
