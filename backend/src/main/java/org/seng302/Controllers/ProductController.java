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
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.AuthenticationTokenManager;
import org.seng302.Tools.UserSearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
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

    Comparator orderBy(String key, String reverse) {
        key = key == null ? "productCode" : key;
        reverse = reverse == null ? "false" : reverse;

        Comparator sort;
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
    JSONArray retrieveCatalogue(@PathVariable Long id,
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

            Comparator sort = orderBy(orderBy, reverse);
            catalogue.sort(sort);

            catalogue = UserSearchHelper.getPageInResults(catalogue, page, resultsPerPage);

            JSONArray responseBody = new JSONArray();
            for (Product product: catalogue) {
                responseBody.appendElement(product.constructJSONObject());
            }
            return responseBody;
        }
    }
}
