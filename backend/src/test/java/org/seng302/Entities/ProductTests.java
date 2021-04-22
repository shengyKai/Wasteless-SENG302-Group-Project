package org.seng302.Entities;

import org.junit.jupiter.api.*;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.ProductRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductTests {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;

    private Product testProduct = new Product();

    private User testUser1;
    private Business testBusiness1;
    private Business testBusiness2;
    private Business testBusiness3;

    @BeforeAll
    public void setUp() throws ParseException {
        businessRepository.deleteAll();

        testUser1 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser1 = userRepository.save(testUser1);
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);
    }

    @AfterEach
    public void cleanUp() {
        productRepository.deleteAll();
    }

    /**
     * Test that a product object can be created and all its attributes are what they are expected to be.
     */
    @Test
    public void createValidProduct() {
        Product product = new Product.Builder().withProductCode("Orange-69").withName("Fresh Orange")
                .withDescription("This is a fresh orange").withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness1).build();
        productRepository.save(product);
        assertEquals(product.getProductCode(), "Orange-69");
        assertEquals(product.getName(), "Fresh Orange");
        assertEquals(product.getDescription(), "This is a fresh orange");
        assertEquals(product.getRecommendedRetailPrice(), new BigDecimal("2.01"));
    }

    /**
     * Test that the auto generated IDs of the products are unique and a newly created product's id does not match
     * the previously created product's id.
     */
    @Test
    public void checkTwoProductsDoNotHaveTheSameIDs() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-69").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1).build();
        Product product2 = new Product.Builder().withProductCode("Orange-70").withName("Fresh Orange")
                .withDescription("This is a fresh orange").withRecommendedRetailPrice("2.02")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        productRepository.save(product2);
        assertNotEquals(product1.getID(), product2.getID());
    }

    /**
     * Test the date that is generated when a product is created is today (when the product is created).
     */
    @Test
    public void checkDate() {
        Date before = new Date();
        Product product = new Product.Builder().withProductCode("NathanApple-69").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1).build();
        productRepository.save(product);
        Date after = new Date();
        Date productDate = product.getCreated();
        assertFalse(productDate.after(after));
        assertFalse(productDate.before(before));
    }

    /**
     * Check that a product with the same code as one in a catalogue cannot be added to said catalogue, because two
     * products with the same code cannot be in the same catalogue.
      */
    @Test
    public void checkNoTwoSameProductCodesWithinSameCatalogue() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-69").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1).build();
        Product product2 = new Product.Builder().withProductCode("NathanApple-69").withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe").withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        assertThrows(Exception.class, () -> productRepository.save(product2));
    }

    /**
     * Checks that two products with different product codes can be added to the same catalogue.
     */
    @Test
    public void checkTwoDifferentProductCodesWithinSameCatalogue() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-69").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1).build();
        Product product2 = new Product.Builder().withProductCode("NathanApple-70").withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe").withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        productRepository.save(product2);
    }

    /**
     * //TODO Add docstring
     */
    @Test
    public void checkTheProductIsConnectedToTheBusinessCatalogue() {

    }


    /**
     * //TODO Add docstring
     */
    @Test
    public void createAValidListOfProductsInACatalogue() {

    }
}
