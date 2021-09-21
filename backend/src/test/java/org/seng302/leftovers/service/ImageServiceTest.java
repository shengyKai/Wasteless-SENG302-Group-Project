package org.seng302.leftovers.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.tools.ImageTools;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

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
    @Mock
    private BufferedImage mockSourceImage;
    @Mock
    private BufferedImage mockScaledImage;
    @Mock
    private InputStream mockInputStream;
    @Mock
    private InputStream mockScaledInputStream;

    private ImageService imageService;

    private MockedStatic<ImageIO> imageIO;
    private MockedStatic<ImageTools> imageTools;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockImage.getFilename()).thenReturn("test_filename.png");
        when(mockImage.getFilenameThumbnail()).thenReturn("test_filename.thumb.png");

        imageIO = Mockito.mockStatic(ImageIO.class);
        imageIO.when(() -> ImageIO.read(mockInputStream)).thenReturn(mockSourceImage);

        imageTools = Mockito.mockStatic(ImageTools.class);
        imageTools.when(() -> ImageTools.generateThumbnail(mockSourceImage)).thenReturn(mockScaledImage);
        imageTools.when(() -> ImageTools.writeImage(eq(mockScaledImage), any())).thenReturn(mockScaledInputStream);

        // Return input value
        when(imageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        imageService = new ImageServiceImpl(imageRepository, storageService);
    }

    @AfterEach
    void tearDown() {
        imageIO.close();
        imageTools.close();
    }

    @ParameterizedTest
    @CsvSource({
            "image/png,png",
            "image/jpeg,jpg"
    })
    void create_validImageType_imageSaved(String contentType, String expectedFormat) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(mockInputStream);
        when(file.getContentType()).thenReturn(contentType);

        Image image = assertDoesNotThrow(() -> imageService.create(file));

        verify(imageRepository, times(1)).save(image);

        assertTrue(image.getFilename().endsWith("." + expectedFormat));

        String[] parts = image.getFilename().split("\\.");
        assertEquals(String.join(".", parts[0], "thumb", parts[1]), image.getFilenameThumbnail());

        imageTools.verify(() -> ImageTools.writeImage(mockScaledImage, expectedFormat));

        verify(storageService, times(1)).store(file.getInputStream(), image.getFilename());
        verify(storageService, times(1)).store(mockScaledInputStream, image.getFilenameThumbnail());
    }

    @Test
    void create_alreadyThumbnailSized_noExtraThumbnailSaved() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(mockInputStream);
        when(file.getContentType()).thenReturn("image/png");

        imageTools.when(() -> ImageTools.generateThumbnail(mockSourceImage)).thenReturn(mockSourceImage);

        Image image = assertDoesNotThrow(() -> imageService.create(file));

        verify(imageRepository, times(1)).save(image);
        assertEquals(image.getFilename(), image.getFilenameThumbnail());

        imageTools.verify(times(0), () -> ImageTools.writeImage(any(), any()));

        verify(storageService, times(1)).store(file.getInputStream(), image.getFilename());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"image/gif", "image/svg+xml", "image/strange"})
    void create_invalidImageType_400Exception(String contentType) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(mockInputStream);
        when(file.getContentType()).thenReturn(contentType);

        var exception = assertThrows(ValidationResponseException.class, () -> imageService.create(file));
        assertEquals("Invalid image format. Must be jpeg or png", exception.getMessage());

        verify(imageRepository, times(0)).save(any());
        verify(storageService, times(0)).store(any(), any());
    }

    @Test
    void create_invalidImage_400Exception() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(mockInputStream);
        when(file.getContentType()).thenReturn("image/png");

        imageIO.when(() -> ImageIO.read(mockInputStream)).thenReturn(null);

        var exception = assertThrows(ValidationResponseException.class, () -> imageService.create(file));
        assertEquals("Invalid image provided", exception.getMessage());

        verify(imageRepository, times(0)).save(any());
        verify(storageService, times(0)).store(any(), any());
    }

    @Test
    void delete_imageProvided_imageAndThumbnailDeleted() {
        imageService.delete(mockImage);
        verify(imageRepository, times(1)).delete(mockImage);
        verify(storageService, times(1)).deleteOne(mockImage.getFilename());
        verify(storageService, times(1)).deleteOne(mockImage.getFilenameThumbnail());
    }

    @Test
    void delete_noThumbnail_thumbnailNotDeleted() {
        when(mockImage.getFilenameThumbnail()).thenReturn(null);

        imageService.delete(mockImage);
        verify(storageService, times(0)).deleteOne(mockImage.getFilenameThumbnail());
    }

    @Test
    void delete_thumbnailSameAsNormal_fileNotDeletedTwice() {
        String filename = mockImage.getFilename();
        when(mockImage.getFilenameThumbnail()).thenReturn(filename);

        imageService.delete(mockImage);
        verify(storageService, times(1)).deleteOne(mockImage.getFilename());
    }
}
