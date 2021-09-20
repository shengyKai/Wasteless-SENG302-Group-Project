package org.seng302.leftovers.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.service.ImageService;
import org.seng302.leftovers.service.StorageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageControllerTest {

    @Autowired
    private ImageRepository imageRepository;

    @Mock
    private ImageRepository mockImageRepository;
    @Mock
    private ImageService mockImageService;
    @Mock
    private StorageService mockStorageService;

    @Mock
    private Image mockImage;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;
    private Image testImage;
    private MockMvc mockMvc;

    @BeforeEach
    private void setUp() {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        imageRepository.deleteAll();
        testImage = new Image("anImage.png", "anImage_thumbnail.png");
        imageRepository.save(testImage);

        when(mockImage.getFilename()).thenReturn("foo.png");
        when(mockImageRepository.findByFilename(not(eq("foo.png")))).thenReturn(Optional.empty());
        when(mockImageRepository.findByFilename("foo.png")).thenReturn(Optional.of(mockImage));

        when(mockImage.getFilenameThumbnail()).thenReturn("foo.thumb.png");
        when(mockImageRepository.findByFilenameThumbnail(not(eq("foo.thumb.png")))).thenReturn(Optional.empty());
        when(mockImageRepository.findByFilenameThumbnail("foo.thumb.png")).thenReturn(Optional.of(mockImage));

        ImageController imageController = new ImageController(mockImageRepository, mockImageService, mockStorageService);
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    @AfterEach
    void tearDown() {
        authenticationTokenManager.close();
        imageRepository.deleteAll();
    }

    /**
     * Checks that an image that exists within the database can be retrieves
     */
    @Test
    void getImage_ImageExist_getExpectedImage() {
        Image actualImage = imageRepository.getImageById(testImage.getID());
        assertEquals(testImage.getID(), actualImage.getID());
    }

    /**
     * Checks that an image that does not exists cannot be retrieved
     */
    @Test
    void getImage_ImageDoesNotExist_406ResponseException() {
        imageRepository.delete(testImage);
        long id = testImage.getID();
        assertThrows(DoesNotExistResponseException.class, () -> {
            imageRepository.getImageById(id);
        });
    }

    @Test
    void requestImage_notAuthorised_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        mockMvc.perform(get("/media/images/foo.png"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void requestImage_imageNotFound_406Response() throws Exception {
        // Not sure why everything after and including the final dot is removed
        mockMvc.perform(get("/media/images/notfound.png."))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(mockImageRepository, times(1)).findByFilename("notfound.png");
        verify(mockImageRepository, times(1)).findByFilenameThumbnail("notfound.png");
        verify(mockStorageService, times(0)).load(any());
    }

    @Test
    void requestImage_requestedImage_200Response() throws Exception {
        mockMvc.perform(get("/media/images/foo.png."))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockImageRepository, times(1)).findByFilename("foo.png");
        verify(mockImageRepository, times(0)).findByFilenameThumbnail(any());
        verify(mockStorageService, times(1)).load("foo.png");
    }

    @Test
    void requestImage_requestedThumbnail_200Response() throws Exception {
        mockMvc.perform(get("/media/images/foo.thumb.png."))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockImageRepository, times(1)).findByFilename("foo.thumb.png");
        verify(mockImageRepository, times(1)).findByFilenameThumbnail("foo.thumb.png");
        verify(mockStorageService, times(1)).load("foo.thumb.png");
    }
}
