package org.seng302.leftovers.service;

import org.seng302.leftovers.entities.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing the validation, creation and deletion of images.
 * Works as a layer on top of the raw StorageService
 */
public interface ImageService {
    /**
     * Validates and creates a new image from a user uploaded file
     * If the uploaded file type is invalid then a 400 exception will be thrown
     * @param file User uploaded file
     * @return Created image entity
     */
    Image create(MultipartFile file);

    /**
     * Deletes an image entity and deletes the underlying file
     * Should be used instead of ImageRepository.delete
     * @param image Image to delete
     */
    void delete(Image image);
}
