package org.seng302.entities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @BeforeAll
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

    }

    /**
     * Tests that an image object can be successfully save and retrieved from the database (image repository)
     */
    @Test
    void createImageObject_imageInDatabase_imageCreated() {

    }

    /**
     * Tests that an image object can have its directory (filename) changed
     */
    @Test
    void setFilename_changeFilename_filenameChanged() {

    }

    /**
     * Tests that an image object cannot have its directory (filename) set to null
     */
    @Test
    void setFilename_changeFilenameToNull_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory (filename) to include a space
     */
    @Test
    void setFilename_changeFilenameHaveSpace_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory (filename) exclude a dot. This is required to define what
     * type of photo it is
     */
    @Test
    void setFilename_changeFilenameHaveNoDot_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory (filename) include an invalid or unsupported image type
     * after the dot, such as dad in this case.
     */
    @Test
    void setFilename_changeFilenameInvalidImageType_BadRequestException() {

    }

    /**
     * Tests that an image object can have its directory (filename) set to include supported photo types such as PNG, JPG, etc.
     */
    @Test
    void setFilename_changeFilenameValidTypes_filenameChanged() {

    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory (filename).
     */
    @Test
    void setFilename_changeFilenameForwardSlashesBeforeDot_BadRequestException() {

    }

    /**
     * Tests that an image object can have its directory for the thumbnail changed
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnail_filenameThumbnailChanged() {

    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to null
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToNull_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail to include a space
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveSpace_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail exclude a dot. This is required to define
     * what type of photo it is
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveNoDot_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail include an invalid or unsupported image
     * type after the dot, such as dad in this case.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailInvalidImageType_BadRequestException() {

    }

    /**
     * Tests that an image object can have its directory for the thumbnail set to include supported photo types such as
     * PNG, JPG, etc.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailValidTypes_filenameThumbnailChanged() {

    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory for the thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailForwardSlashesBeforeDot_BadRequestException() {

    }

    /**
     * Tests that an image object must have a underscore followed before thumbnail before defining the filename for the
     * thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailNotIncludeUnderscoreThumbnail_BadRequestException() {

    }

    //TODO Discuss with team if we should include tests and validation to test if the image at the directory (filename) actually exists
}

