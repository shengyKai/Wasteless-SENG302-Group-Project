package org.seng302.leftovers.controllers;

// import net.bytebuddy.asm.Advice.OffsetMapping.Sort;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.exceptions.BusinessNotFoundException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.service.StorageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SortOrder;

import java.util.*;

/**
 * This class handles requests for retrieving and saving products
 */
@RestController
public class ProductController {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private static final Logger logger = LogManager.getLogger(ProductController.class.getName());
    @Autowired
    public ProductController(ProductRepository productRepository, BusinessRepository businessRepository, StorageService storageService, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.storageService = storageService;
        this.imageRepository = imageRepository;
    }

    /**
     * REST GET method to retrieve all the products with a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products")
    public JSONObject retrieveCatalogue(@PathVariable Long id,
                                       HttpServletRequest request,
                                       @RequestParam(required = false) String orderBy,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer resultsPerPage,
                                       @RequestParam(required = false) Boolean reverse) {
                                        
        logger.info("Get catalogue by business id.");
        AuthenticationTokenManager.checkAuthenticationToken(request);
                            
        logger.info(() -> String.format("Retrieving catalogue from business with id %d.", id));
        Optional<Business> business = businessRepository.findById(id);

        List<Sort.Order> sortOrder;
        Sort.Direction direction = SearchHelper.getSortDirection(reverse);

        sortOrder = List.of(new Sort.Order(direction, orderBy).ignoreCase());

    
        if (business.isEmpty()) {
            BusinessNotFoundException notFound = new BusinessNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            // business.get().checkSessionPermissions(request);
            PageRequest pageablePage = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
            Page<Product> catalogue = productRepository.getAllByBusiness(business.get(), pageablePage);

            JSONArray responseBody = new JSONArray();
            for (Product product: catalogue) {
                responseBody.appendElement(product.constructJSONObject());
            }
            JSONObject json = new JSONObject();
            json.put("count", catalogue.getTotalElements());
            json.put("results", responseBody);
            return json;
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
            logger.info(() -> String.format("Adding product to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);

            business.checkSessionPermissions(request);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product creation info not provided");
            }
            String productCode = productInfo.getAsString("id");

            if (productRepository.findByBusinessAndProductCode(business, productCode).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists with product code in this catalogue \"" + productCode + "\"");
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
     * PUT endpoint for modifying an existing product in a businesses catalogue.
     * @param businessId The business ID to select a product from
     * @param productCode Product to modify
     * @param productInfo Replacement product data
     * @param request Additional information about the request
     */
    @PutMapping("/businesses/{businessId}/products/{productCode}")
    public void modifyProduct(@PathVariable Long businessId, @PathVariable String productCode, @RequestBody JSONObject productInfo, HttpServletRequest request) {
        try {
            logger.info(() -> String.format("Modifying product in business (businessId=%d, productCode=%s).", businessId, productCode));
            AuthenticationTokenManager.checkAuthenticationToken(request);

            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            Product product = productRepository.getProduct(business, productCode);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No request body provided");
            }

            String newProductCode = productInfo.getAsString("id");
            if (!Objects.equals(productCode, newProductCode) && productRepository.findByBusinessAndProductCode(business, newProductCode).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists with product code in this catalogue \"" + productCode + "\"");
            }

            product.setProductCode(newProductCode);
            product.setName(productInfo.getAsString("name"));
            product.setDescription(productInfo.getAsString("description"));
            product.setManufacturer(productInfo.getAsString("manufacturer"));
            product.setRecommendedRetailPrice(productInfo.getAsString("recommendedRetailPrice"));
            productRepository.save(product);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Matches up the businessID, productID and imageID to find the image of a product to be deleted. Only business
     * owners can delete product images and they must be within their own product catalogue.
     * @param businessId the ID of the business
     * @param productId the ID of the product
     * @param imageId the ID of the image
     */
    @DeleteMapping("/businesses/{businessId}/products/{productId}/images/{imageId}")
    public void deleteProductImage(@PathVariable Long businessId, @PathVariable String productId,
                                   @PathVariable Long imageId,
                                   HttpServletRequest request) {
        logger.info(() -> String.format("Deleting image with id %d from the product %s within the business's catalogue %d",
                imageId, productId, businessId));

        Business business = businessRepository.getBusinessById(businessId); // get the business + sanity checks
        business.checkSessionPermissions(request); // Can this user do this action

        Product product = productRepository.getProduct(business, productId); // get the product + sanity checks
        Image image = imageRepository.getImageByProductAndId(product, imageId); // get the image + sanity checks

        product.removeProductImage(image);
        imageRepository.delete(image);
        storageService.deleteOne(image.getFilename());

        productRepository.save(product);
    }

    @PostMapping("/businesses/{businessId}/products/{productCode}/images")
    public ResponseEntity<Void> uploadImage(@PathVariable Long businessId, @PathVariable String productCode, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding product image to business (businessId=%d, productCode=%s).", businessId, productCode));
            Business business = businessRepository.getBusinessById(businessId);


            business.checkSessionPermissions(request);

            // Will throw 406 response status exception if product does not exist
            Product product = productRepository.getProduct(business, productCode);

            validateImage(file);

            String filename = UUID.randomUUID().toString();
            if ("image/jpeg".equals(file.getContentType())) {
                filename += ".jpg";
            } else if ("image/png".equals(file.getContentType())) {
                filename += ".png";
            } else {
                assert false; // We've already validated the image type so this should not be possible.
            }

            Image image = new Image(null, null);
            image.setFilename(filename);
            image = imageRepository.save(image);
            product.addProductImage(image);
            productRepository.save(product);
            storageService.store(file, filename);             //store the file using storageService

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
    /**
     * Sets the given image as the primary image for the given product
     * Only business administrators can perform this action.
     * @param businessId the ID of the business
     * @param productId the ID of the product
     * @param imageId the ID of the image
     */
    @PutMapping("/businesses/{businessId}/products/{productId}/images/{imageId}/makeprimary")
    public void makeImagePrimary(@PathVariable Long businessId,@PathVariable String productId,
                                 @PathVariable Long imageId,
                                 HttpServletRequest request ) {
        // get business + sanity
        Business business = businessRepository.getBusinessById(businessId);
        // check user priv
        business.checkSessionPermissions(request);

        // get product + sanity
        Product product = productRepository.getProduct(business, productId);
        // get image + sanity
        Image image = imageRepository.getImageByProductAndId(product, imageId);

        List<Image> images = product.getProductImages(); // get the images so we can manipulate them
        // If the given image is already the primary image, return
        if (images.get(0).getID().equals(image.getID())) {
            return;
        }

        images.remove(image); // pop the image from the list
        images.add(0, image); // append to the start of the list
        product.setProductImages(images); // apply the changes
        productRepository.save(product);
        logger.info(() -> String.format("Set Image %d of product \"%s\" as the primary image", image.getID(), product.getName()));
    }

    public void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if(contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image format. Must be jpeg or png");
        }
    }
}
