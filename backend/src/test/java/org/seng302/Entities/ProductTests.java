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
import java.util.List;

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

    private User testUser1;
    private Business testBusiness1;
    private Business testBusiness2;

    /**
     * Created a business objects for testing
     */
    public void createTestBusinesses() {
        System.out.println("Creating businesses");
        Thread.dumpStack();
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("BusinessName1")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);

        testBusiness2 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description 2")
                .withName("BusinessName2")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness2 = businessRepository.save(testBusiness2);
    }

    @BeforeAll
    public void setUp() throws ParseException {
        businessRepository.deleteAll();
        userRepository.deleteAll();

        // Add a test user that will be the owner of both businesses
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
        createTestBusinesses();
    }

    @AfterAll
    public void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
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
     * Check that two products with the same code can be added to different catalogues
     */
    @Test
    public void checkTwoSameProductCodesWithinDifferentCatalogues() {
        Product product1 = new Product.Builder()
                .withProductCode("NathanApple-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("NathanApple-69")
                .withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe")
                .withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness2)
                .build();
        productRepository.save(product1);
        assertDoesNotThrow(() -> productRepository.save(product2));
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
     * Checks the product is connected to the business's catalogue by checking the mentioned product is the same as
     * the one in the catalogue.
     */
    @Test
    public void checkTheProductIsConnectedToTheBusinessCatalogue() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-70").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        testBusiness1 = businessRepository.findByName("BusinessName1");
        List<Product> catalogue = testBusiness1.getCatalogue();
        assertEquals(product1.getID(), catalogue.get(0).getID());
    }

    /**
     * Checks that several products are successfully added to the business's catalogue
     */
    @Test
    public void createAValidListOfProductsInACatalogue() {
        Product product1 = new Product.Builder().withProductCode("NathanApple-70").withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple").withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1).build();
        Product product2 = new Product.Builder().withProductCode("NathanApple-71").withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple too").withRecommendedRetailPrice("9000.04")
                .withBusiness(testBusiness1).build();
        Product product3 = new Product.Builder().withProductCode("NathanApple-72").withName("The Nathan Apple Three")
                .withDescription("Ever wonder why Nathan has an apple too maybe").withRecommendedRetailPrice("9000.05")
                .withBusiness(testBusiness1).build();
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        testBusiness1 = businessRepository.findByName("BusinessName1");
        List<Product> catalogue = testBusiness1.getCatalogue();
        assertEquals(3, catalogue.size());
    }

    /**
     * Checks that deleting a business while it still has remaining products doesn't result in an error
     */
    @Test
    public void testDeletingBusinessWithProducts() {
        Business tempBusinessInitial = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("This business will be deleted")
                .withName("Temp Name")
                .withPrimaryOwner(testUser1)
                .build();
        Business tempBusiness = businessRepository.save(tempBusinessInitial);
        Product product1 = new Product.Builder()
                .withProductCode("NathanApple-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(tempBusiness).build();
        productRepository.save(product1);

        assertDoesNotThrow(() -> businessRepository.delete(tempBusiness));
    }
}
