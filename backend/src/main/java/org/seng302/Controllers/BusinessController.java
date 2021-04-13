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
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
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
}
