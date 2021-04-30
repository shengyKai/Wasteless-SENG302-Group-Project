package org.seng302.entities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImageTests {
    @Autowired
    ImageRepository imageRepository;

    Image testImage;

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
        createTestImage();
    }

    @AfterAll
    void teardown() {
        imageRepository.deleteAll();
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
        try {
            testImage.setFilename(null);
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to null
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToNull_BadRequestException() {
        try {
            testImage.setFilenameThumbnail(null);
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No thumbnail filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) set to an empty string
     */
    @Test
    void setFilename_changeFilenameToEmpty_BadRequestException() {
        try {
            testImage.setFilename("");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An empty filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to an empty string
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToEmpty_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An empty thumbnail filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) to include a space
     */
    @Test
    void setFilename_changeFilenameHaveSpace_BadRequestException() {
        try {
            testImage.setFilename("Happy Meal.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Spaces are not allowed in the filename", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) exclude a dot. This is required to define what
     * type of photo it is
     */
    @Test
    void setFilename_changeFilenameHaveNoDot_BadRequestException() {
        try {
            testImage.setFilename("Connorpng");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) include an invalid or unsupported image type
     * after the dot, such as dad in this case.
     */
    @Test
    void setFilename_changeFilenameInvalidImageType_BadRequestException() {
        try {
            testImage.setFilename("Connor.yup");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object can have its directory (filename) set to include supported photo types such as PNG, JPG, etc.
     */
    @Test
    void setFilename_changeFilenameValidTypes_filenameChanged() {
        String filenamePNG = "happyboi.png";
        String filenameJPG = "happyboi.jpg";
        testImage.setFilename(filenamePNG);
        assertEquals(filenamePNG, testImage.getFilename());
        testImage.setFilename(filenameJPG);
        assertEquals(filenameJPG, testImage.getFilename());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory (filename).
     */
    @Test
    void setFilename_changeFilenameForwardSlashesBeforeDot_BadRequestException() {
        try {
            testImage.setFilename("connor./png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail to include a space
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveSpace_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Happy Meal_thumbnail.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Spaces are not allowed in the thumbnail filename", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail exclude a dot. This is required to define
     * what type of photo it is
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveNoDot_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Connorthumbnailpng");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail include an invalid or unsupported image
     * type after the dot, such as dad in this case.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailInvalidImageType_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Connor_thumbnail.yup");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object can have its directory for the thumbnail set to include supported photo types such as
     * PNG, JPG, etc.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailValidTypes_filenameThumbnailChanged() {
        String filenameThumbnailPNG = "happyboi_thumbnail.png";
        String filenameThumbnailJPG = "happyboi_thumbnail.jpg";
        testImage.setFilenameThumbnail(filenameThumbnailPNG);
        assertEquals(filenameThumbnailPNG, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailJPG);
        assertEquals(filenameThumbnailJPG, testImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory for the thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailForwardSlashesBeforeDot_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("connor_thumbnail./png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object must have a underscore followed before thumbnail before defining the filename for the
     * thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailNotIncludeUnderscoreThumbnail_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("helppppppppppp.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("The thumbnail filename does not contain an _thumbnail", e.getReason());
        } catch (Exception e) { fail(); }
    }

    //TODO Discuss with team if we should include tests and validation to test if the image at the directory (filename) actually exists
}

