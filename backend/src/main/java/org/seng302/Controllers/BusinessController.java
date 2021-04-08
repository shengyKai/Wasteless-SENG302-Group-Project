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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    //TODO Could this be part of the location class for ease of use?
    /**
     * Parses the address part of the business info and constructs a location object
     * @param businessInfo Business info
     * @return A new Location object containing the business address
     */
    private Location parseLocation(JSONObject businessInfo) {
        JSONObject businessLocation = new JSONObject((Map<String, ?>) businessInfo.get("address")) ;

        Location address = new Location.Builder()
                .inCountry(businessLocation.getAsString("country"))
                .inCity(businessLocation.getAsString("city"))
                .inRegion(businessLocation.getAsString("region"))
                .onStreet(businessLocation.getAsString("streetName"))
                .atStreetNumber(businessLocation.getAsString("streetNumber"))
                .withPostCode(businessLocation.getAsString("postcode"))
                .inSuburb("suburb")
                .build();
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
}
