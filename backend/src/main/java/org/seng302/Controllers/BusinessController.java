package org.seng302.Controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
public class BusinessController {
    private final BusinessRepository _businessRepository;
    private final UserRepository _userRepository;
    private static final Logger logger = LogManager.getLogger(BusinessController.class.getName());

    public BusinessController(BusinessRepository businessRepository, UserRepository userRepository) {
        this._businessRepository = businessRepository;
        this._userRepository = userRepository;
    }

    /**
     * Parses the address part of the business info and constructs a location object using the Location class function
     * @param businessInfo Business info
     * @return A new Location object containing the business address
     */
    private Location parseLocation(JSONObject businessInfo) {
        JSONObject businessLocation = new JSONObject((Map<String, ?>) businessInfo.get("address")) ;
        Location address = Location.parseLocationFromJson(businessLocation);
        return address;
    }

    /**
     * POST endpoint for registering a new business.
     * Ensures that the given primary business owner is an existing User.
     * Adds the business to the database if all of the business information is valid.
     * @param businessInfo A Json object containing all of the business's details from the registration form.
     */
    @PostMapping("/businesses")
    public ResponseEntity register(@RequestBody JSONObject businessInfo, HttpServletRequest req) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req);

            // Make sure this is an existing user ID
            Optional<User> primaryOwner = _userRepository.findById(Long.parseLong((businessInfo.getAsString("primaryAdministratorId"))));

            if (!primaryOwner.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The given PrimaryBusinessOwner does not exist");
            } else if (!AuthenticationTokenManager.sessionCanSeePrivate(req, primaryOwner.get().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You don't have permission to set the provided Primary Owner");
            }

            Location address = parseLocation(businessInfo); // Get the address for the business
            // Build the business
            Business newBusiness = new Business.Builder()
                    .withPrimaryOwner(primaryOwner.get())
                    .withBusinessType(businessInfo.getAsString("businessType"))
                    .withDescription(businessInfo.getAsString("description"))
                    .withName(businessInfo.getAsString("name"))
                    .withAddress(address)
                    .build();

            _businessRepository.save(newBusiness); // Save the new business
            logger.info("Business has been registered");
            return new ResponseEntity(HttpStatus.CREATED);

        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }
    }

    /**
     * This method searchs for the business with the given ID in the database. If the business exists, return a JSON representation of the
     * business. If the business does not exists, send a response with status code 406/Not Acceptable.
     * @return JSON representation of the business.
     */
    @GetMapping("/businesses/{id}")
    JSONObject getUserById(@PathVariable Long id, HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info(String.format("Retrieving business with ID %d.", id));
        Optional<Business> business = _businessRepository.findById(id);
        if (business.isEmpty()) {
            ResponseStatusException notFoundException = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format("No business with ID %d.", id));
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        }
        return business.get().constructJson(true);
    };


    /**
     * PUT endpoint for making an individual an administrator of a business
     * Only the business primary owner can do this
     * @param userInfo The info containing the UserId for the User to make an administrator
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/makeAdministrator")
    public void makeAdmin(@RequestBody JSONObject userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            loggedInUserHasPermissions(req, business);
            User user = getUser(userInfo); // Get the user to promote

            business.addAdmin(user);
            _businessRepository.save(business);

        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }
    }

    /**
     * PUT endpoint for removing the administrator status of a user from a business
     * Only the business primary owner can do this
     * @param userInfo The info containing the UserId for the User to remove from the list of administrators
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/removeAdministrator")
    public void removeAdmin(@RequestBody JSONObject userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            loggedInUserHasPermissions(req, business);
            User user = getUser(userInfo); // Get the user to demote

            business.removeAdmin(user);
            _businessRepository.save(business);

        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }

    }

    /**
     * Gets a user from the database and performs sanity checks to ensure User is not null
     * Throws a ResponseStatusException if the user does not exist
     * @param userInfo Data containing the Id of the user to find
     * @return A user of given UserId
     */
    private User getUser(@RequestBody JSONObject userInfo) {
        // Check a valid Long id is given in the request
        if (!userInfo.containsKey("userId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not find a user id in the request");
        }

        Long userId = userInfo.getAsNumber("userId").longValue();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not find a user id in the request");
        }
        // check the requested user exists
        Optional<User> user = _userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The given user does not exist");
        }
        return user.get();
    }

    /**
     * Gets a business from the database matching a given Business Id
     * Performs sanity checks to ensure the business is not null
     * Throws ResponseStatusException if business does not exist
     * @param businessId The id of the business to retrieve
     * @return The business matching the given Id
     */
    private Business getBusiness(Long businessId) {
        // check business exists
        Optional<Business> business = _businessRepository.findById(businessId);
        if (!business.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "The given business does not exist");
        }
        return business.get();
    }

    /**
     * Determines if the currently logged in user is the Primary Owner of the given business
     * @param req The httpRequest
     * @param business The business to compare
     * @return User is Primary owner
     */
    private boolean loggedInUserIsOwner(HttpServletRequest req, Business business) {
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("accountId");
        return userId == business.getPrimaryOwner().getUserID();
    }

    /**
     * Determines if the currently logged in user is Primary Owner OR an application admin
     * Throws a ResponseStatusException if they are neither Primary Owner OR an application admin
     * @param req The httpRequest
     * @param business The business to compare
     */
    private void loggedInUserHasPermissions(HttpServletRequest req, Business business) {
        // check user is owner
        if (!(loggedInUserIsOwner(req, business) || AuthenticationTokenManager.sessionCanSeePrivate(req, null))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action");
        }
    }
}
