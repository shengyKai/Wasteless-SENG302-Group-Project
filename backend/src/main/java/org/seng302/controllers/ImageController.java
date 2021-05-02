package org.seng302.controllers;

import org.seng302.entities.Image;
import org.seng302.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class ImageController {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Gets an image from the database that matches a given image Id. This method preforms a sanity check to ensure the
     * image does exist and if not throws a not accepted response status exception.
     * @param imageRepository the image repository that connects to the database
     * @param imageId the id of the image
     * @return the image object that matches the given Id
     */
    //TODO add unit tests
    public static Image getImage(ImageRepository imageRepository, Long imageId) {
        Optional<Image> image = imageRepository.findById(imageId);
        if (!image.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "the given image does not exist");
        }
        return image.get();
    }
}
