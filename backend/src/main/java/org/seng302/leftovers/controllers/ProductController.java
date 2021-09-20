package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.product.ProductResponseDTO;
import org.seng302.leftovers.dto.product.ProductFilterOption;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.product.UpdateProductDTO;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.service.ImageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This class handles requests for retrieving and saving products
 */
@RestController
public class ProductController {
    @Autowired
    private ObjectMapper objectMapper;

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private static final Logger logger = LogManager.getLogger(ProductController.class.getName());
    @Autowired
    public ProductController(ProductRepository productRepository, BusinessRepository businessRepository, ImageService imageService, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    private static final Set<String> VALID_ORDERINGS = Set.of("name", "description", "manufacturer","recommendedRetailPrice", "created", "productCode");

    /**
     * REST GET method to retrieve all the products with a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products")
    public ResultPageDTO<ProductResponseDTO> retrieveCatalogue(@PathVariable Long id,
                                                               HttpServletRequest request,
                                                               @RequestParam(required = false) String orderBy,
                                                               @RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer resultsPerPage,
                                                               @RequestParam(required = false) Boolean reverse) {

        logger.info("Retrieving catalogue from business with id={}", id);
        AuthenticationTokenManager.checkAuthenticationToken(request);

        Business business = businessRepository.getBusinessById(id);

        List<Sort.Order> sortOrder = getSortOrder(orderBy, reverse);

        business.checkSessionPermissions(request);
        PageRequest pageablePage = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
        Page<Product> catalogue = productRepository.getAllByBusiness(business, pageablePage);
        return new ResultPageDTO<>(catalogue.map(ProductResponseDTO::new));
    }

    /**
     * GET endpoint to retrieve products from a business with searching
     * @param id of business
     * @param request HTTP request
     * @param searchQuery to match searchBy field
     * @param page of results to return
     * @param resultsPerPage products per page and amount to return
     * @param searchBy field of product to search
     * @param reverse most or least relevant
     * @param orderBy field to sort results by
     * @return List of products
     */
    @GetMapping("/businesses/{id}/products/search")
    public ResultPageDTO<ProductResponseDTO> retrieveCatalogueSearch(@PathVariable Long id,
                                                                     HttpServletRequest request,
                                                                     @RequestParam(required = false) String searchQuery,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer resultsPerPage,
                                                                     @RequestParam(required = false) List<String> searchBy,
                                                                     @RequestParam(required = false) Boolean reverse,
                                                                     @RequestParam(required = false) String orderBy
                                              ) {
        logger.info("Get catalogue by business id.");
        AuthenticationTokenManager.checkAuthenticationToken(request);

        logger.info(() -> String.format("Retrieving catalogue from business with id %d.", id));
        Business business = businessRepository.getBusinessById(id);

        // Convert searchBy into ProductFilterOption type and check valid
        searchBy = Optional.ofNullable(searchBy).orElse(List.of());

        Set<ProductFilterOption> searchSet;
        try {
            searchSet = objectMapper.convertValue(searchBy, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search option provided", e);
        }

        business.checkSessionPermissions(request);
        List<Sort.Order> sortOrder = getSortOrder(orderBy, reverse);
        PageRequest pageablePage = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
        Specification<Product> prodSpec = SearchHelper.constructSpecificationFromProductSearch(business, searchQuery, searchSet);

        Page<Product> catalogue = productRepository.findAll(prodSpec, pageablePage);
        return new ResultPageDTO<>(catalogue.map(ProductResponseDTO::new));
    }

    /**
     * How to sort your results
     * @param orderBy Field to sort results by
     * @param reverse Whether results should be forward or backward
     * @return sortOrder
     */
    private List<Sort.Order> getSortOrder(String orderBy, Boolean reverse) {
        orderBy = Optional.ofNullable(orderBy).orElse("productCode");
        if (!VALID_ORDERINGS.contains(orderBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OrderBy term " + orderBy + " is invalid");
        }

        List<Sort.Order> sortOrder;
        Sort.Direction direction = SearchHelper.getSortDirection(reverse);
        sortOrder = List.of(new Sort.Order(direction, orderBy ).ignoreCase());
        return sortOrder;
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
    public void addProductToBusiness(@PathVariable Long id, @RequestBody UpdateProductDTO productInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding product to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);

            business.checkSessionPermissions(request);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product creation info not provided");
            }

            if (productRepository.findByBusinessAndProductCode(business, productInfo.getId()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists with product code in this catalogue \"" + productInfo.getId() + "\"");
            }

            Product product = new Product.Builder()
                    .withProductCode(productInfo.getId())
                    .withName(productInfo.getName())
                    .withDescription(productInfo.getDescription())
                    .withManufacturer(productInfo.getManufacturer())
                    .withRecommendedRetailPrice(productInfo.getRecommendedRetailPrice())
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
    public void modifyProduct(@PathVariable Long businessId, @PathVariable String productCode, @RequestBody UpdateProductDTO productInfo, HttpServletRequest request) {
        try {
            logger.info(() -> String.format("Modifying product in business (businessId=%d, productCode=%s).", businessId, productCode));
            AuthenticationTokenManager.checkAuthenticationToken(request);

            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            Product product = productRepository.getProduct(business, productCode);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No request body provided");
            }

            if (!Objects.equals(productCode, productInfo.getId()) && productRepository.findByBusinessAndProductCode(business, productInfo.getId()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists with product code in this catalogue \"" + productCode + "\"");
            }

            product.setProductCode(productInfo.getId());
            product.setName(productInfo.getName());
            product.setDescription(productInfo.getDescription());
            product.setManufacturer(productInfo.getManufacturer());
            product.setRecommendedRetailPrice(productInfo.getRecommendedRetailPrice());
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

        imageService.delete(image);

        product.removeImage(image);
        productRepository.save(product);
    }

    /**
     * Adds a new image to the product's image list
     * @param businessId Business of product
     * @param productCode Unique within business product code
     * @param file Uploaded image
     * @return Empty response with 201 if successful
     */
    @PostMapping("/businesses/{businessId}/products/{productCode}/images")
    public ResponseEntity<Void> uploadImage(@PathVariable Long businessId, @PathVariable String productCode, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding product image to business (businessId=%d, productCode=%s).", businessId, productCode));
            Business business = businessRepository.getBusinessById(businessId);


            business.checkSessionPermissions(request);

            // Will throw 406 response status exception if product does not exist
            Product product = productRepository.getProduct(business, productCode);

            Image image = imageService.create(file);

            product.addImage(image);
            productRepository.save(product);

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

        List<Image> images = product.getImages(); // get the images so we can manipulate them
        // If the given image is already the primary image, return
        if (images.get(0).getID().equals(image.getID())) {
            return;
        }

        images.remove(image); // pop the image from the list
        images.add(0, image); // append to the start of the list
        product.setImages(images); // apply the changes
        productRepository.save(product);
        logger.info(() -> String.format("Set Image %d of product \"%s\" as the primary image", image.getID(), product.getName()));
    }
}
