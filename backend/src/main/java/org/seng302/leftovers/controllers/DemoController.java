package org.seng302.leftovers.controllers;

import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.seng302.datagenerator.*;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
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
    private final InventoryItemRepository inventoryItemRepository;
    private final SaleItemRepository saleItemRepository;
    private final EntityManager entityManager;
    private static final Logger logger = LogManager.getLogger(DemoController.class.getName());

    public DemoController(UserRepository userRepository, BusinessRepository businessRepository, ProductRepository productRepository, InventoryItemRepository inventoryItemRepository, SaleItemRepository saleItemRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.saleItemRepository = saleItemRepository;
        this.entityManager = entityManager;
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
    public void loadDemoData(HttpServletRequest request) throws Exception {
        try {
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
            for (Business owned : businessesOwned) {
                if (owned.getName().equals("BUSINESS_NAME")) {
                    business = owned;
                }
            }

            // Construct demo business and save it to the repository
            if (business == null) {
                business = new Business.Builder()
                        .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                        .withDescription("DESCRIPTION")
                        .withName("BUSINESS_NAME")
                        .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
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
            product = productRepository.save(product);


            // Construct a demo inventory item for said product
            LocalDate today = LocalDate.now();
            InventoryItem inventoryItem = new InventoryItem.Builder()
                    .withProduct(product)
                    .withQuantity(1000)
                    .withPricePerItem("10")
                    .withTotalPrice((String) null)
                    .withManufactured(today.minusDays(1))
                    .withSellBy(today.plusDays(1))
                    .withBestBefore(today.plusDays(2))
                    .withExpires(today.plusDays(3))
                    .build();
            inventoryItem = inventoryItemRepository.save(inventoryItem);

            // Construct a demo sale item for the inventory item
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withQuantity(10)
                    .withMoreInfo("Some more info")
                    .withCloses(today.plusDays(1).toString())
                    .withPrice("1")
                    .build();
            saleItemRepository.save(saleItem);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
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

    /**
     * DTO representing request body for generator options
     */
    @Getter
    @ToString
    public static class GeneratorRequestDTO {
      private int userCount = 0;
      private int businessCount = 0;
      private int productCount = 0;
      private int inventoryItemCount = 0;
      private int cardCount = 0;
      private int saleItemCount = 0;

      private List<Long> userInitial = new ArrayList<>();
      private List<Long> businessInitial = new ArrayList<>();
      private List<Long> productInitial = new ArrayList<>();
      private List<Long> inventoryItemInitial = new ArrayList<>();
      private List<Long> saleItemInitial = new ArrayList<>();
      private Boolean generateProductImages = false;
      private Boolean generateBusinessImages = false;
      private int businessImageMin = 0;
      private int businessImageMax = 3;
    }


    /**
     * Generates a set of demo data (Using the more advanced generators)
     * @param options Contains the quantity field which determines the number of products to generates
     * @return JSON including generated Users, Businesses and Products IDs
     */
    @PostMapping("/demo/generate")
    public JSONObject generate(HttpServletRequest request, @RequestBody GeneratorRequestDTO options) {
        AuthenticationTokenManager.checkAuthenticationTokenDGAA(request);

        List<Long> allUsers = options.getUserInitial();
        List<Long> allBusinesses = options.getBusinessInitial();
        List<Long> allProducts = options.getProductInitial();
        List<Long> allInventoryItems = options.getInventoryItemInitial();

        JSONObject json = new JSONObject();
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            var userGenerator = new UserGenerator(connection);
            var businessGenerator = new BusinessGenerator(connection);
            var productGenerator = new ProductGenerator(connection);
            var inventoryItemGenerator = new InventoryItemGenerator(connection);
            var saleItemGenerator = new SaleItemGenerator(connection);
            var cardGenerator = new MarketplaceCardGenerator(connection);
            var businessImageGenerator = new BusinessImageGenerator(connection);

            List<Long> userIds = userGenerator.generateUsers(options.userCount);
            allUsers.addAll(userIds);

            List<Long> businessIds = businessGenerator.generateBusinesses(allUsers, options.getBusinessCount());
            allBusinesses.addAll(businessIds);

            List<Long> productIds = productGenerator.generateProducts(allBusinesses, options.productCount, options.getGenerateProductImages());
            allProducts.addAll(productIds);

            List<Long> inventoryIds = inventoryItemGenerator.generateInventoryItems(allProducts, options.getInventoryItemCount());
            allInventoryItems.addAll(inventoryIds);

            List<Long> saleItemIds = saleItemGenerator.generateSaleItems(allInventoryItems, options.getSaleItemCount());

            List<Long> cardIds = cardGenerator.generateCards(allUsers, options.getCardCount());

            if (options.getGenerateBusinessImages()) {
                businessImageGenerator.generateBusinessImages(allBusinesses, options.getBusinessImageMin(), options.getBusinessImageMax());
            }

            json.appendField("generatedUsers", userIds);
            json.appendField("generatedBusinesses", businessIds);
            json.appendField("generatedProducts", productIds);
            json.appendField("generatedInventoryItems", inventoryIds);
            json.appendField("generatedSaleItems", saleItemIds);
            json.appendField("generatedCards", cardIds);

        });
        return json;
    }


}