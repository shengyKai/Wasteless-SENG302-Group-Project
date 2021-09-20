package org.seng302.leftovers.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ImageDTO;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.service.ImageService;
import org.seng302.leftovers.service.StorageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
public class ImageController {
    private static final Logger logger = LogManager.getLogger(ImageController.class);

    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final StorageService storageService;

    @Autowired
    public ImageController(ImageRepository imageRepository, ImageService imageService, StorageService storageService) {
        this.imageRepository = imageRepository;
        this.imageService = imageService;
        this.storageService = storageService;
    }

    @PostMapping("/media/images")
    public ImageDTO createImage(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info("Creating new image");

            Image image = imageService.create(file);

            response.setStatus(201);
            return new ImageDTO(image);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @GetMapping("/media/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable("imageName") String imageName, HttpServletRequest session) {
        logger.info(() -> String.format("Fetching image with name=%s", imageName));
        AuthenticationTokenManager.checkAuthenticationToken(session);

        Optional<Image> retrievedImage = imageRepository.findByFilename(imageName);
        if (retrievedImage.isEmpty()) {
            retrievedImage = imageRepository.findByFilenameThumbnail(imageName);
        }
        if (retrievedImage.isEmpty()) {
            throw new DoesNotExistResponseException(Image.class);
        }
        Resource file = storageService.load(imageName);
        return ResponseEntity.status(HttpStatus.OK).contentType(guessMediaType(imageName)).body(file);
    }

    private MediaType guessMediaType(String filename) {
        if (filename.endsWith(".jpg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        throw new InternalErrorResponseException("Couldn't determine image type");
    }
}
