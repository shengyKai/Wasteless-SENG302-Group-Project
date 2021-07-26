package org.seng302.datagenerator;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class ProductImageGeneratorTest {

    private static final String PRODUCT_NOUNS_FILE = "product-nouns.txt";
    private static final String PRODUCT_ADJECTIVES_FILE = "product-adjectives.txt";

    private Connection conn;
    // LocationGenerator is called by UserGenerator or BusinessGenerator, so it has to be called with either of them
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private ProductImageGenerator productImageGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImageRepository imageRepository;

    private List<String> productNouns = ExampleDataFileReader.readExampleDataFile(PRODUCT_NOUNS_FILE);
    private List<String> productAdjectives = ExampleDataFileReader.readExampleDataFile(PRODUCT_ADJECTIVES_FILE);


    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates generators
        this.userGenerator = new UserGenerator(conn);
        this.businessGenerator = new BusinessGenerator(conn);
        this.productGenerator = new ProductGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        imageRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();

        conn.close();
    }

    /**
     * Sets up a test user and a test business
     * Useful for creating test products
     * @return List of generated business IDs
     */
    private List<Long> createTestBusiness() {
        var userIds = userGenerator.generateUsers(1);
        return businessGenerator.generateBusinesses(userIds, 1);
    }

    /**
     * Checks that the required fields within the image table are not null using an SQL query.
     * @param productId The ID of the generated product
     * @param expectedCount The expected number of images for the product
     * @throws SQLException SQL fails
     */
    private void checkRequiredFieldsNotNull(Long productId, int expectedCount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM image WHERE image_id = ? AND filename IS NOT NULL AND image_order IS NOT NULL"
        );
        stmt.setObject(1, productId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != expectedCount) {
            fail();
        }
    }

    /**
     * Queries that database to find out how many image entries are in the database
     * @return the number of image entries in the database
     */
    private Long getNumImagesInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM image");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    @Test
    void generateOneProduct_generateImage_oneImageEntryGenerated() throws SQLException {
        var businessIds = createTestBusiness();
        var productIds = productGenerator.generateProducts(businessIds, 1, true);

        if (productIds.size() != 1) {
            fail();
        }
        long productId = productIds.get(0);

        checkRequiredFieldsNotNull(productId, 1);
        Assertions.assertEquals(1, getNumImagesInDB());
    }

    @Test
    void generateMultipleProduct_generateImage_multipleImageEntryGenerated() throws SQLException {
        var businessIds = createTestBusiness();
        var productIds = productGenerator.generateProducts(businessIds, 10, true);

        if (productIds.size() != 10) {
            fail();
        }
        for (var productId : productIds) {
            checkRequiredFieldsNotNull(productId, 1);
        }
        Assertions.assertEquals(10, getNumImagesInDB());
    }

    @Test
    void generateOneProduct_notGenerateImage_noImageEntryGenerated() throws SQLException {
        var businessIds = createTestBusiness();
        var productIds = productGenerator.generateProducts(businessIds, 1, false);

        if (productIds.size() != 1) {
            fail();
        }
        for (var productId : productIds) {
            checkRequiredFieldsNotNull(productId, 0);
        }
        Assertions.assertEquals(0, getNumImagesInDB());
    }

    @Test
    void generateMultipleProduct_notGenerateImage_noImageEntryGenerated() throws SQLException {
        var businessIds = createTestBusiness();
        var productIds = productGenerator.generateProducts(businessIds, 10, false);

        if (productIds.size() != 10) {
            fail();
        }
        for (var productId : productIds) {
            checkRequiredFieldsNotNull(productId, 0);
        }
        Assertions.assertEquals(0, getNumImagesInDB());
    }

    // Refer to manual testing for further tests

}
