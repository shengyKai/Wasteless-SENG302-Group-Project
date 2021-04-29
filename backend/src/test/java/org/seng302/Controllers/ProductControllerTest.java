package org.seng302.Controllers;

import org.junit.jupiter.api.Test;

public class ProductControllerTest {

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
}
