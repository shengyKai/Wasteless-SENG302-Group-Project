package org.seng302.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.seng302.entities.Product;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;

public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private ProductRepository productRepository;

    private User testUser;
    private Business testBusiness;
    private Product testProduct;

    /**
     * Creates a user, business and product objects for use within the unit tests, where the
     * user is the owner of the business and the product exists within the business's
     * product catalogue.
     * @throws ParseException from the date attribute within the user object
     */
    private void setUpTestObjects() throws ParseException {
        testUser = new User.Builder()
                .withFirstName("Fergus")
                .withMiddleName("Connor")
                .withLastName("Hitchcock")
                .withNickName("Ferg")
                .withEmail("fergus.hitchcock@gmail.com")
                .withPassword("IDoLikeBreaks69#H3!p")
                .withBio("Did you know I had a second last name Yarker")
                .withDob("1999-07-17")
                .withPhoneNumber("+64 27 370 2682")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .build();
        userRepository.deleteAll();
        userRepository.save(testUser);

        testBusiness = new Business.Builder()
                .withName("Help Industries")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .withBusinessType("Accommodation and Food Services")
                .withDescription("Helps industries hopefully")
                .withPrimaryOwner(testUser)
                .build();
        businessRepository.deleteAll();
        businessRepository.save(testBusiness);


        testProduct = new Product.Builder()
                .withProductCode("PieceOfFish69")
                .withName("A Piece of Fish")
                .withDescription("A fish but only a piece of it remains")
                .withManufacturer("Tokyo Fishing LTD")
                .withRecommendedRetailPrice("3.20")
                .withBusiness(testBusiness)
                .build();
        productRepository.deleteAll();
        productRepository.save(testProduct);

        testBusiness.addToCatalogue(testProduct);
        businessRepository.save(testBusiness);
    }

    @BeforeEach
    void setUp() throws ParseException {
        setUpTestObjects();
    }

    @AfterAll
    void tearDown() {
        userRepository.deleteAll();
        businessRepository.deleteAll();
        productRepository.deleteAll();
    }

    /**
     * Tests using the delete product image method to see if a product with an image will have its image deleted.
     * This is done by calling the API endpoint to delete a product image and checking if it not longer has an image
     */
    @Test
    void deleteProductImage_hasImage_imageDeleted() {

    }

    /**
     * Tests using the delete product image method to see if a product without an image will respond with the not
     * acceptable response code.
     */
    @Test
    void deleteProductImage_noImage_406Response() {

    }

    /**
     * Tests using the delete image method to see if a request with an invalid business ID will return a not acceptable
     * response code.
     */
    @Test
    void deleteProductImage_invalidBusinessID_406Response() {

    }

    /**
     * Tests using the delete image method to see if a request with an invalid product ID will return a not acceptable
     * response code.
     */
    @Test
    void deleteProductImage_invalidProductID_406Response() {

    }

    /**
     * Tests using the delete product image method to see if a product with a valid authentication token has permission
     * to delete an image.
     */
    @Test
    void deleteProductImage_validAuthToken_hasPermission() {

    }

    /**
     * Tests using the delete product image method to see if a user without a authentication token cannot delete an image.
     * A unauthorised response code should be given back when the API endpoint is called under these conditions.
     */
    @Test
    void deleteProductImage_noAuthToken_401Response() {

    }

    /**
     * Tests using the delete product image method to see if a user without an invalid authentication token cannot delete
     * an image. A unauthorised response code should be given back when the API endpoint is called under these conditions.
     */
    @Test
    void deleteProductImage_invalidAuthToken_401Response() {

    }

    /**
     * Tests using the delete product image method to see if a user who is not a DGAA and just a regular user cannot
     * delete the image.
     */
    @Test
    void deleteProductImage_isNotDGAA_403Response() {

    }

    /**
     * Tests using the delete image method to see if a DGAA without being a business owner can delete images products.
     */
    @Test
    void deleteProductImage_isDGAA_imageDeleted() {

    }

    /**
     * Tests using the delete image method to see if the business administrator can delete images within there
     * businesses product catalogue.
     */
    @Test
    void deleteProductImage_isBusinessAdmin_imageDeleted() {

    }

    /**
     * Tests using the delete image method to see if a user who is not a business administrator cannot delete images
     * from products.
     */
    @Test
    void deleteProductImage_notBusinessAdmin_403Response() {

    }

    /**
     * Tests using the delete image method to see if a user who is a business administrator cannot delete images from
     * products that exist in a different business's product catalogue
     */
    @Test
    void deleteProductImage_isBusinessAdminForWrongCatalogue_403Response() {

    }

    /**
     * Tests using the make image primary method will make the given image the primary image
     */
    @Test
    public void makeImagePrimary_valid_sets_image_primary() {

    }

    /**
     * Tests that using the make image primary method with a business that does not exist,
     * a 406 response is thrown
     */
    @Test
    public void makeImagePrimary_InvalidBusinessId_406Response() {

    }
    /**
     * Tests that using the make image primary method with a product that does not exist,
     * a 406 response is thrown
     */
    @Test
    public void makeImagePrimary_InvalidProductId_406Response() {

    }

    /**
     * Tests that using the make image primary method with a Image that does not exist,
     * a 406 response is thrown
     */
    @Test
    public void makeImagePrimary_InvalidImageId_406Response() {

    }

    /**
     * Tests that using the make image primary method with a session that is not a business admin,
     * a 403 response is thrown
     */
    @Test
    public void makeImagePrimary_NotBusinessAdmin_403Response() {

    }
    /**
     * Tests that using the make image primary method with a session that is not logged in,
     * a 401 response is thrown
     */
    @Test
    public void makeImagePrimary_NoSession_401Response() {

    }

    /**
     * Tests using the ake image primary method to see if a user who is a business administrator cannot edit images from
     * products that exist in a different business's product catalogue
     */
    @Test
    void makeImagePrimary_isBusinessAdminForWrongCatalogue_403Response() {

    }




}
