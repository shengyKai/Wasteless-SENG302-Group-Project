package org.seng302.Controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Entities.Account;
import org.seng302.Persistence.AccountRepository;
import org.seng302.Tools.AuthenticationTokenManager;
import org.seng302.Tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class handles requests from the appliation's login page.
 */
@RestController
public class LoginController {

    private final AccountRepository accountRepository;
    private static final Logger logger = LogManager.getLogger(LoginController.class.getName());

    @Autowired
    public LoginController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Check that the entered email exists and the password is correct. If they are, respond with status code 200: OK and
     * a cookie with the user's authentication token. If they are not, respond with status code 400: Bad request.
     * @param userinfo JSON object with user's email and password.
     * @param response HTTP response.
     */
    @PostMapping("/login")
    public void login(@RequestBody JSONObject userinfo, HttpServletRequest request, HttpServletResponse response) {
        String email = userinfo.getAsString("email");
        String password = userinfo.getAsString("password");
        Account matchingAccount = accountRepository.findByEmail(email);
        if (matchingAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no account associated with this email");
        } else {
            try {
                PasswordAuthenticator.verifyPassword(password, matchingAccount.getAuthenticationCode());
            } catch (ResponseStatusException responseError) {
                throw responseError;
            }
            if (matchingAccount.isIsDGAA()) {
                AuthenticationTokenManager.setAuthenticationTokenDGAA(request);
            }
            try {
                response.setStatus(200);
                response.setContentType("application/json");
                String authToken = AuthenticationTokenManager.setAuthenticationToken(request, response);
                response.getWriter().write(authToken);
                response.getWriter().flush();
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }
}
