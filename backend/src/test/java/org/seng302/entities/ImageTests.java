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

    /**
     * Creates a test image to be used within these tests
     */
    void createTestImage() {
        Image image = new Image("help.png");
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
     * Tests that a image object can be created and the attributes are what they are to be expected
     */
    @Test
    void createImageObject_imageCreated_imageCreated() {

    }

    /**
     * Tests that a image object can be successfully save and retrieved from the database (image repository)
     */
    @Test
    void createImageObject_imageInDatabase_imageCreated() {

    }

    /**
     * Tests that an image object can have its directory changed
     */
    @Test
    void setImageDirectory_changeDirectory_directoryChanged() {

    }

    /**
     * Tests that an image object cannot have its directory set to null
     */
    @Test
    void setImageDirectory_changeDirectoryToNull_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory to include a space
     */
    @Test
    void setImageDirectory_changeDirectoryHaveSpace_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory exclude a dot. This is required to define what type of
     * photo it is
     */
    @Test
    void setImageDirectory_changeDirectoryHaveNoDot_BadRequestException() {

    }

    /**
     * Tests that an image object cannot have its directory include an invalid or unsupported image type after the dot,
     * such as dad in this case.
     */
    @Test
    void setImageDirectory_changeDirectoryInvalidImageType_BadRequestException() {

    }

    /**
     * Tests that an image object can have its directory set to include supported photo types such as PNG, JPG, etc.
     */
    @Test
    void setImageDirectory_changeDirectoryValidTypes_DirectoryChanged() {

    }

    /**
     * Tests that an image object cannot have a forward slash after the dot
     */
    void setImageDirectory_changeDirectoryForwardSlashesBeforeDot_BadRequestException() {

    }

    //TODO Discuss with team if we should include tests and validation to test if the image at the directory actually exists
}

