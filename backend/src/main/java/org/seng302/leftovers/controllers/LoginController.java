package org.seng302.leftovers.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Account;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.AccountRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * This class handles requests from the appliation's login page.
 */
@RestController
public class LoginController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public LoginController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * DTO representing a login request
     */
    @Getter
    @ToString
    public static class LoginRequestDTO {
        @NotNull
        private String email;
        @NotNull
        private String password;
    }

    /**
     * DTO representing the response of a login request
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class LoginResponseDTO {
        private Long userId;
    }


    /**
     * Check that the entered email exists and the password is correct. If they are, respond with status code 200: OK and
     * a cookie with the user's authentication token. If they are not, respond with status code 400: Bad request.
     * @param userInfo JSON object with user's email and password.
     * @param response HTTP response.
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO userInfo, HttpServletRequest request, HttpServletResponse response) {
        Account matchingAccount = accountRepository.findByEmail(userInfo.getEmail());
        if (matchingAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no account associated with this email");
        } else {
            PasswordAuthenticator.verifyPassword(userInfo.getPassword(), matchingAccount.getAuthenticationCode());

            Optional<User> accountAsUser = userRepository.findById(matchingAccount.getUserID());
            AuthenticationTokenManager.setAuthenticationToken(request, response, accountAsUser.orElseThrow());

            return new LoginResponseDTO(matchingAccount.getUserID());
        }
    }
}
