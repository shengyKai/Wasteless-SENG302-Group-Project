package org.seng302.Controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Entities.Business;
import org.seng302.Entities.Product;
import org.seng302.Exceptions.BusinessNotFoundException;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.ProductRepository;
import org.seng302.Tools.AuthenticationTokenManager;
import org.seng302.Tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This class handles requests for retrieving and saving products
 */
@RestController
public class ProductController {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private static final Logger logger = LogManager.getLogger(ProductController.class.getName());

    @Autowired
    public ProductController(ProductRepository productRepository, BusinessRepository businessRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Sort products by a key. Can reverse results.
     * @param key Key to order products by.
     * @param reverse Reverse results.
     * @return Product Comparator
     */
    Comparator<Product> sortProducts(String key, String reverse) {
        key = key == null ? "productCode" : key;
        reverse = reverse == null ? "false" : reverse;

        Comparator<Product> sort;
        switch (key) {
            case "name":
                sort = Comparator.comparing(Product::getName);
                break;

            case "description":
                sort = Comparator.comparing(Product::getDescription);
                break;

            case "manufacturer":
                sort = Comparator.comparing(Product::getManufacturer);
                break;

            case "recommendedRetailPrice":
                sort = Comparator.comparing(Product::getRecommendedRetailPrice);
                break;

            case "created":
                sort = Comparator.comparing(Product::getCreated);
                break;

            default:
                sort = Comparator.comparing(Product::getProductCode);
                break;
        }

        if (!reverse.isEmpty() && reverse.equals("true")) {
            sort = sort.reversed();
        }

        return sort;
    }

    /**
     * REST GET method to retrieve all the products with a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products")
    private JSONArray retrieveCatalogue(@PathVariable Long id,
                                HttpServletRequest request,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) String page,
                                @RequestParam(required = false) String resultsPerPage,
                                @RequestParam(required = false) String reverse) {

        logger.info("Get catalogue by business id.");
        AuthenticationTokenManager.checkAuthenticationToken(request);

        logger.info(String.format("Retrieving catalogue from business with id %d.", id));
        Optional<Business> business = businessRepository.findById(id);
        if (business.isEmpty()) {
            BusinessNotFoundException notFound = new BusinessNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            business.get().checkSessionPermissions(request);

            List<Product> catalogue = business.get().getCatalogue();

            Comparator<Product> sort = sortProducts(orderBy, reverse);
            catalogue.sort(sort);

            catalogue = SearchHelper.getPageInResults(catalogue, page, resultsPerPage);

            JSONArray responseBody = new JSONArray();
            for (Product product: catalogue) {
                responseBody.appendElement(product.constructJSONObject());
            }
            return responseBody;
        }
    }

    /**
     * REST GET method to retrieve the number of products in a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products/count")
    private JSONObject retrieveCatalogueCount(@PathVariable Long id,
                                      HttpServletRequest request) {

        AuthenticationTokenManager.checkAuthenticationToken(request);

        Optional<Business> business = businessRepository.findById(id);

        if(business.isEmpty()) {
            BusinessNotFoundException notFound = new BusinessNotFoundException();
            logger.error(notFound.getMessage());
            throw new BusinessNotFoundException();
        } else {
            business.get().checkSessionPermissions(request);

            List<Product> catalogue = business.get().getCatalogue();

            JSONObject responseBody = new JSONObject();
            responseBody.put("count", catalogue.size());

            return responseBody;
        }
    }


    /**
     * POST endpoint for adding a product to a businesses catalogue.
     * This is only accessible to the DGAA, the business owner or a business admin.
     * @param id The business id to add a product to
     * @param productInfo The request body that should contain the product information
     * @param request Additional information about the request
     * @param response The response to this request
     */
    @PostMapping("/businesses/{id}/products")
    public void addProductToBusiness(@PathVariable Long id, @RequestBody JSONObject productInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(String.format("Adding product to business (businessId=%d).", id));
            Business business = getBusiness(id);

            business.checkSessionPermissions(request);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product creation info not provided");
            }
            String productCode = productInfo.getAsString("id");

            if (productRepository.findByBusinessAndProductCode(business, productCode) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product already exists with product code in this catalogue \"" + productCode + "\"");
            }

            Product product = new Product.Builder()
                    .withProductCode(productCode)
                    .withName(productInfo.getAsString("name"))
                    .withDescription(productInfo.getAsString("description"))
                    .withManufacturer(productInfo.getAsString("manufacturer"))
                    .withRecommendedRetailPrice(productInfo.getAsString("recommendedRetailPrice"))
                    .withBusiness(business)
                    .build();
            productRepository.save(product);

            response.setStatus(201);
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * Checks that the provided JSON object exists and has all the fields that are required
     * @param requestBody The request body to validate
     * @param requiredFields The fields that are required to exist in the request body
     * @throws ResponseStatusException If the requestBody is invalid
     */
    private void checkObjectHasFields(JSONObject requestBody, List<String> requiredFields) throws ResponseStatusException {
        if (requestBody == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must contain a JSON body");
        }

        for (String field : requiredFields) {
            if (requestBody.get(field) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must have a \"" + field + "\" field");
            }
        }
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
        Optional<Business> business = businessRepository.findById(businessId);
        if (!business.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "The given business does not exist");
        }
        return business.get();
    }
}
