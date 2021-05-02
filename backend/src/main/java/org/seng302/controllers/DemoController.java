package org.seng302.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.seng302.entities.Product;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class for API endpoints used for developing/demoing application. Remove from release.
 */
@RestController
public class DemoController {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(DemoController.class.getName());

    public DemoController(UserRepository userRepository, BusinessRepository businessRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.productRepository = productRepository;
    }

    /**
     * This method checks the requests credentials to see if the request comes from an admin. If the request's credentials
     * can be verified and the request comes from an admin then demo data will be loaded into the database.
     * Responses:
     * 200 - If the request is successful.
     * 401 - If no authentication token is present, or the authentication token is invalid.
     * 403 - If a valid authentication token is present, but it is for an account that is not an admin.
     * @param request The HTTP request to use for verifying the account's credentials.
     */
    @PutMapping("/demo/load")
    public void loadDemoData(HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        if (!AuthenticationTokenManager.sessionIsAdmin(request)) {
            ResponseStatusException error = new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin accounts can perform demo actions.");
            logger.error(error.getMessage());
            throw error;
        }
        logger.info("Loading demo data...");

        // Load demo users from demo data file and save them to the repository
        List<User> demoUsers = readUserFile();
        for (User user : demoUsers) {
            if (userRepository.findByEmail(user.getEmail()) == null) {
                userRepository.save(user);
            }
        }

        User user = userRepository.findByEmail("123andyelliot@gmail.com");

        // Check if the business has already been added to the user's businesses to avoid adding it again
        Business business = null;
        Set<Business> businessesOwned = user.getBusinessesOwned();
        for (Business owned: businessesOwned) {
            if (owned.getName().equals("BUSINESS_NAME")) {
                business = owned;
            }
        }

        // Construct demo business and save it to the repository
        if (business == null) {
            business = new Business.Builder()
                    .withBusinessType("Accommodation and Food Services")
                    .withDescription("DESCRIPTION")
                    .withName("BUSINESS_NAME")
                    .withAddress(Location.covertAddressStringToLocation("100,Clyde Road,Ilam,Christchurch,Japan,Canterbury,8041"))
                    .withPrimaryOwner(user)
                    .build();
            business = businessRepository.save(business);
        }

        // Construct demo product and save it to the repository
        Product product = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(business)
                .build();
        productRepository.save(product);
    }

    /**
     * Reads users from DemoData.tx file to create a list of user objects
     * @return A list of user objects
     */
    private List<User> readUserFile() {
        List<User> userList = new ArrayList<>();
        String row;
        try (BufferedReader csvReader = new BufferedReader(new FileReader("src//main//DemoData.txt"))) {
            while ((row = csvReader.readLine()) != null) {
                String[] userData = row.split("\\|");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(Location.covertAddressStringToLocation(userData[6])).withDob(userData[7]).build();
                userList.add(user);
            }
        } catch (Exception e) {
            logger.error("Could not read demo user data.");
        }
        return userList;
    }

}