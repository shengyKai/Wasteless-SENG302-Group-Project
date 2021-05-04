package org.seng302.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Image;
import org.seng302.persistence.ImageRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.seng302.service.StorageService;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ImageController {
    private static final Logger logger = LogManager.getLogger(org.seng302.controllers.ImageController.class.getName());

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    private StorageService storageService;

    @GetMapping("/media/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable("imageName") String imageName, HttpServletRequest session) throws IOException {
        logger.info("Fetching image with name=" + imageName);
        AuthenticationTokenManager.checkAuthenticationToken(session);

        final Optional<Image> retrievedImage = imageRepository.findByFilename(imageName);
        if (retrievedImage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find image with the given name");
        }

        Resource file = storageService.load(retrievedImage.get().getFilename());
        return ResponseEntity.status(HttpStatus.OK).contentType(guessMediaType(retrievedImage.get().getFilename())).body(file);
    }

    private MediaType guessMediaType(String filename) {
        if (filename.endsWith(".jpg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (filename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't determine image type");
    }
}
