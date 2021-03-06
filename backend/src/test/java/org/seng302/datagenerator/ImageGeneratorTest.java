package org.seng302.datagenerator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes={Main.class})
class ImageGeneratorTest {
    private Connection conn;

    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ImageGenerator imageGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ImageRepository imageRepository;

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
        this.imageGenerator = Mockito.spy(new ImageGenerator(conn));
        doNothing().when(imageGenerator).store(any(), any());

    }

    @AfterEach
    public void teardown() throws SQLException {
        imageRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();

        conn.close();
    }

    @AfterAll
    void finalise() {
        Path root = Paths.get(ExampleDataFileReader.readPropertiesFile("/application.properties").get("storage-directory"));
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    /**
     * Sets up a test user and a test business
     * Useful for creating test products
     * @return List of generated business IDs
     */
    private List<Long> createTestBusinesses(int count) {
        var userIds = createTestUsers(1);
        return businessGenerator.generateBusinesses(userIds, count);
    }

    /**
     * Generates a number of test users
     * @param count The number of users to generate
     * @return List of userIds
     */
    private List<Long> createTestUsers(int count) {
        return userGenerator.generateUsers(count);
    }

    /**
     * Checks that the required fields within the image table are not null using an SQL query.
     * @param businessId The ID of the generated business
     * @param expectedCount The expected number of images for the product
     * @throws SQLException SQL fails
     */
    private void checkRequiredFieldsNotNullBusiness(Long businessId, int expectedCount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM image WHERE business_id = ? AND filename IS NOT NULL AND image_order IS NOT NULL"
        );
        stmt.setObject(1, businessId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        assertEquals(expectedCount, results.getLong(1));
    }

    private void checkRequiredFieldsNotNullUser(Long userId, int expectedCount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM image WHERE user_id = ? AND filename IS NOT NULL AND image_order IS NOT NULL"
        );
        stmt.setObject(1, userId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        assertEquals(expectedCount, results.getLong(1));
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
    void oneBusiness_generateOneImage_oneImageGenerated() throws SQLException {
        var businessIds = createTestBusinesses(1);
        var imageIds = imageGenerator.generateEntityImages(businessIds, 1, 1, ImageGenerator.ImageEntityType.BUSINESS);
        assertEquals(1, imageIds.size());
        checkRequiredFieldsNotNullBusiness(businessIds.get(0), 1);
        assertEquals(1, getNumImagesInDB());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 7, 12})
    void manyBusinesses_generateOneImageEach_manyImageGenerated(int count) throws SQLException {
        var businessIds = createTestBusinesses(count);
        var imageIds = imageGenerator.generateEntityImages(businessIds, 1, 1,ImageGenerator.ImageEntityType.BUSINESS);
        assertEquals(count, imageIds.size());
        for (var businessId : businessIds) {
            checkRequiredFieldsNotNullBusiness(businessId, 1);
        }
        assertEquals(count, getNumImagesInDB());
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5})
    void oneBusiness_generateRangeImagesLowerBound_generatedMoreThanLower(int lowerBound) throws SQLException {
        int upperBound = lowerBound + 2;
        var businessIds = createTestBusinesses(1);
        var imageIds = imageGenerator.generateEntityImages(businessIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.BUSINESS);
        assertTrue(imageIds.size() >= lowerBound);
        assertTrue(getNumImagesInDB() >= lowerBound);
    }
    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10})
    void oneBusiness_generateRangeImagesUpperBound_generatedLessThanUpper(int upperBound) throws SQLException {
        int lowerBound = upperBound - 2;
        var businessIds = createTestBusinesses(1);
        var imageIds = imageGenerator.generateEntityImages(businessIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.BUSINESS);
        assertTrue(imageIds.size() <= upperBound);
        assertTrue(getNumImagesInDB() <= upperBound);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10})
    void manyBusiness_generateRangeImages_generatedWithinRange(int count) throws SQLException {
        var lowerBound = 2;
        var upperBound = 5;
        var businessIds = createTestBusinesses(count);
        var imageIds = imageGenerator.generateEntityImages(businessIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.BUSINESS);
        assertTrue(imageIds.size() <= count * upperBound);
        assertTrue(imageIds.size() >= count * lowerBound);
        assertTrue(getNumImagesInDB() <= (long)count * upperBound);
        assertTrue(getNumImagesInDB() >= (long)count * lowerBound);
    }
    //##################################################

    @Test
    void oneUser_generateOneImage_oneImageGenerated() throws SQLException {
        var userIds = createTestUsers(1);
        var imageIds = imageGenerator.generateEntityImages(userIds, 1, 1, ImageGenerator.ImageEntityType.USER);
        assertEquals(1, imageIds.size());
        checkRequiredFieldsNotNullUser(userIds.get(0), 1);
        assertEquals(1, getNumImagesInDB());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 7, 12})
    void manyUsers_generateOneImageEach_manyImageGenerated(int count) throws SQLException {
        var userIds = createTestUsers(count);
        var imageIds = imageGenerator.generateEntityImages(userIds, 1, 1,ImageGenerator.ImageEntityType.USER);
        assertEquals(count, imageIds.size());
        for (var userId : userIds) {
            checkRequiredFieldsNotNullUser(userId, 1);
        }
        assertEquals(count, getNumImagesInDB());
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5})
    void oneUser_generateRangeImagesLowerBound_generatedMoreThanLower(int lowerBound) throws SQLException {
        int upperBound = lowerBound + 2;
        var userIds = createTestUsers(1);
        var imageIds = imageGenerator.generateEntityImages(userIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.USER);
        assertTrue(imageIds.size() >= lowerBound);
        assertTrue(getNumImagesInDB() >= lowerBound);
    }
    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10})
    void oneUser_generateRangeImagesUpperBound_generatedLessThanUpper(int upperBound) throws SQLException {
        int lowerBound = upperBound - 2;
        var userIds = createTestUsers(1);
        var imageIds = imageGenerator.generateEntityImages(userIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.USER);
        assertTrue(imageIds.size() <= upperBound);
        assertTrue(getNumImagesInDB() <= upperBound);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10})
    void manyUsers_generateRangeImages_generatedWithinRange(int count) throws SQLException {
        var lowerBound = 2;
        var upperBound = 5;
        var businessIds = createTestUsers(count);
        var imageIds = imageGenerator.generateEntityImages(businessIds, lowerBound, upperBound,ImageGenerator.ImageEntityType.USER);
        assertTrue(imageIds.size() <= count * upperBound);
        assertTrue(imageIds.size() >= count * lowerBound);
        assertTrue(getNumImagesInDB() <= (long)count * upperBound);
        assertTrue(getNumImagesInDB() >= (long)count * lowerBound);
    }



}
