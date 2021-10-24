package org.seng302.leftovers.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.leftovers.dto.ImageDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageTests {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Image testImage;

    final private List<String> illegalCharacters = Arrays.asList(".", "\n", "\t", "\\", ",");

    /**
     * Creates a test image to be used within these tests
     */
    void createTestImage() {
        testImage = new Image("help.png", "help_thumbnail.png");
        imageRepository.save(testImage);
    }

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        createTestImage();
    }

    @AfterAll
    void teardown() {
        imageRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Tests that an image object can be created and the attributes are what they are expected to be
     */
    @Test
    void createImageObject_imageCreated_imageCreated() {
        String filename = "boi.png";
        String filenameThumbnail = "boi_thumbnail.png";
        Image newTestImage = new Image(filename, filenameThumbnail);
        assertEquals(filename, newTestImage.getFilename());
        assertEquals(filenameThumbnail, newTestImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object can be successfully save and retrieved from the database (image repository)
     */
    @Test
    void createImageObject_imageInDatabase_imageCreated() {
        Optional<Image> actualImageArray = imageRepository.findById(testImage.getID());
        assertNotNull(actualImageArray);
        Image actualImage = actualImageArray.get();
        assertEquals(testImage.getID(), actualImage.getID());
        assertEquals(testImage.getFilename(), actualImage.getFilename());
        assertEquals(testImage.getFilenameThumbnail(), actualImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object can have its directory (filename) changed
     */
    @Test
    void setFilename_changeFilename_filenameChanged() {
        String filename = "/goodboi/verygoodboi.png";
        testImage.setFilename(filename);
        assertEquals(filename, testImage.getFilename());
    }

    /**
     * Tests that an image object can have its directory for the thumbnail changed
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnail_filenameThumbnailChanged() {
        String filenameThumbnail = "/goodboi/verygoodboi_thumbnail.png";
        testImage.setFilenameThumbnail(filenameThumbnail);
        assertEquals(filenameThumbnail, testImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object cannot have its directory (filename) set to null
     */
    @Test
    void setFilename_changeFilenameToNull_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename(null));
        assertEquals("No filename was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to null
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToNull_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail(null));
        assertEquals("No thumbnail filename was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory (filename) set to an empty string
     */
    @Test
    void setFilename_changeFilenameToEmpty_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename(""));
        assertEquals("An empty filename was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to an empty string
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToEmpty_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail(""));
        assertEquals("An empty thumbnail filename was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory (filename) to include a space
     */
    @Test
    void setFilename_changeFilenameHaveSpace_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename("Happy Meal.png"));
        assertEquals("Spaces are not allowed in the filename", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory (filename) exclude a dot. This is required to define what
     * type of photo it is
     */
    @Test
    void setFilename_changeFilenameHaveNoDot_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename("Connorpng"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory (filename) include an invalid or unsupported image type
     * after the dot, such as dad in this case.
     */
    @Test
    void setFilename_changeFilenameInvalidImageType_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename("Connor.yup"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Tests that an image object can have its directory (filename) set to include supported photo types such as PNG, JPG, etc.
     */
    @Test
    void setFilename_changeFilenameValidTypes_filenameChanged() {
        String filenamePNG = "happyboi.png";
        String filenameJPG = "happyboi.jpg";
        String filenamePNGUpper = "happyboi.PNG";
        String filenameJPGUpper = "happyboi.JPG";
        testImage.setFilename(filenamePNG);
        assertEquals(filenamePNG, testImage.getFilename());
        testImage.setFilename(filenameJPG);
        assertEquals(filenameJPG, testImage.getFilename());
        testImage.setFilename(filenamePNGUpper);
        assertEquals(filenamePNGUpper, testImage.getFilename());
        testImage.setFilename(filenameJPGUpper);
        assertEquals(filenameJPGUpper, testImage.getFilename());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory (filename).
     */
    @Test
    void setFilename_changeFilenameForwardSlashesBeforeDot_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilename("connor./png"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail to include a space
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveSpace_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail("Happy Meal_thumbnail.png"));
        assertEquals("Spaces are not allowed in the thumbnail filename", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail exclude a dot. This is required to define
     * what type of photo it is
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveNoDot_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail("Connorthumbnailpng"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail include an invalid or unsupported image
     * type after the dot, such as dad in this case.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailInvalidImageType_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail("Connor_thumbnail.yup"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Tests that an image object can have its directory for the thumbnail set to include supported photo types such as
     * PNG, JPG, etc.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailValidTypes_filenameThumbnailChanged() {
        String filenameThumbnailPNG = "happyboi_thumbnail.png";
        String filenameThumbnailJPG = "happyboi_thumbnail.jpg";
        String filenameThumbnailPNGUpper = "happyboi_thumbnail.PNG";
        String filenameThumbnailJPGUpper = "happyboi_thumbnail.JPG";
        testImage.setFilenameThumbnail(filenameThumbnailPNG);
        assertEquals(filenameThumbnailPNG, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailJPG);
        assertEquals(filenameThumbnailJPG, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailPNGUpper);
        assertEquals(filenameThumbnailPNGUpper, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailJPGUpper);
        assertEquals(filenameThumbnailJPGUpper, testImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory for the thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailForwardSlashesBeforeDot_BadRequestException() {
        var e = assertThrows(ValidationResponseException.class, () -> testImage.setFilenameThumbnail("connor_thumbnail./png"));
        assertEquals("An invalid image format was provided", e.getMessage());
    }

    /**
     * Checks that there cannot be two images with the same filename within the database.
     */
    @Test
    void createImage_ViolateUniqueFilename_Exception() {
        try {
            testImage = new Image("help.png", "original_thumbnail.png");
            imageRepository.save(testImage);
            fail();
        } catch (Exception e) { assertEquals(DataIntegrityViolationException.class, e.getClass()); }
    }

    @Test
    void createImage_validParameters_creationTimeSet() {
        var before = Instant.now();
        testImage = new Image("help.png", "original_thumbnail.png");
        var after = Instant.now();
        assertFalse(testImage.getCreated().isBefore(before));
        assertFalse(testImage.getCreated().isAfter(after));
    }

    @Test
    void imageDTO_withImage_expectedFieldsReturned() {
        var image = mock(Image.class);
        when(image.getID()).thenReturn(6L);
        when(image.getFilename()).thenReturn("foo.png");
        when(image.getFilenameThumbnail()).thenReturn("bar.png");

        var json = objectMapper.convertValue(new ImageDTO(image), JSONObject.class);
        assertEquals(image.getID(), json.get("id"));
        assertEquals( "/media/images/foo.png", json.get("filename"));
        assertEquals("/media/images/bar.png", json.get("thumbnailFilename"));
        assertEquals(3, json.size());
    }

    /**
     * Creates a test user
     * @return Test user
     */
    private User createUser() {
        return new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withDob("2001-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
    }

    /**
     * Creates a test business with the provided owner
     * @param owner Business owner
     * @return Created business
     */
    private Business createBusiness(User owner) {
        return new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Joe's Garage")
                .withPrimaryOwner(owner)
                .build();
    }

    /**
     * Creates a test product with the provided business
     * @param business Business to assign to
     * @return Created product
     */
    private Product createProduct(Business business) {
        return new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(business)
                .build();
    }

    @Test
    void getAttachment_noAttachment_nullReturned() {
        var image = imageRepository.save(new Image("foo.png", "bar.png"));
        assertNull(image.getAttachment());
    }


    @Test
    void getAttachment_savedBusiness_businessReturned() {
        var image = imageRepository.save(new Image("foo.png", "bar.png"));

        var business = createBusiness(userRepository.save(createUser()));
        List<Image> images = business.getImages();
        images.add(image);
        business.setImages(images);
        businessRepository.save(business);

        try (Session session = sessionFactory.openSession()) {
            image = session.get(Image.class, image.getID());
            assertTrue(image.getAttachment() instanceof Business);
            assertEquals(business.getId(), ((Business)image.getAttachment()).getId());
        }
    }

    @Test
    void getAttachment_savedProduct_productReturned() {
        var image = imageRepository.save(new Image("foo.png", "bar.png"));

        var product = createProduct(businessRepository.save(createBusiness(userRepository.save(createUser()))));
        product.addImage(image);
        productRepository.save(product);

        try (Session session = sessionFactory.openSession()) {
            image = session.get(Image.class, image.getID());
            assertTrue(image.getAttachment() instanceof Product);
            assertEquals(product.getID(), ((Product)image.getAttachment()).getID());
        }
    }



    @Test
    void save_savedBusinessAndProduct_failsToSave() {
        var image = imageRepository.save(new Image("foo.png", "bar.png"));

        var business = createBusiness(userRepository.save(createUser()));
        List<Image> images = business.getImages();
        images.add(image);
        business.setImages(images);
        business = businessRepository.save(business);

        var product = createProduct(business);
        product.addImage(image);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.save(product));
    }
}

