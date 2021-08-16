package org.seng302.leftovers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.persistence.ImageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ImageServiceTest {
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private Image mockImage;

    private ImageService imageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockImage.getFilename()).thenReturn("test_filename.png");

        // Return input value
        when(imageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        imageService = new ImageServiceImpl(imageRepository, storageService);
    }

    /**
     * Creates a mock multipart file
     * @param contentType Content type for the generated file
     * @return Mock file with provided content type
     */
    private MultipartFile createMockUpload(String contentType) {
        return new MockMultipartFile("file", "filename.txt", contentType, new byte[100]);
    }

    @ParameterizedTest
    @CsvSource({
            "image/png,.png",
            "image/jpeg,.jpg"
    })
    void create_validImageType_imageSaved(String contentType, String expectedExtension) {
        MultipartFile file = createMockUpload(contentType);

        Image image = assertDoesNotThrow(() -> imageService.create(file));

        verify(imageRepository, times(1)).save(image);

        assertTrue(image.getFilename().endsWith(expectedExtension));
        assertNull(image.getFilenameThumbnail());

        verify(storageService, times(1)).store(file, image.getFilename());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"image/gif", "image/svg+xml", "image/strange"})
    void create_invalidImageType_400Exception(String contentType) {
        MultipartFile file = createMockUpload(contentType);

        var exception = assertThrows(ResponseStatusException.class, () -> imageService.create(file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid image format. Must be jpeg or png", exception.getReason());

        verify(imageRepository, times(0)).save(any());
        verify(storageService, times(0)).store(any(), any());
    }

    @Test
    void delete_imageProvided_imageDeleted() {
        imageService.delete(mockImage);
        verify(imageRepository, times(1)).delete(mockImage);
        verify(storageService, times(1)).deleteOne(mockImage.getFilename());
    }
}
