package org.seng302.leftovers.service;

import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Implementation of StorageService
 */
@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final StorageService storageService;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository, StorageService storageService) {
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    /**
     * Validates the provided image's content type and generates an appropriate filename
     * @param file Uploaded file
     * @return Generated filename
     */
    private String generateFilename(MultipartFile file) {
        String filename = UUID.randomUUID().toString();
        if ("image/jpeg".equals(file.getContentType())) {
            filename += ".jpg";
        } else if ("image/png".equals(file.getContentType())) {
            filename += ".png";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image format. Must be jpeg or png");
        }
        return filename;
    }

    /**
     * Creates a new image entity from a user upload
     * If the uploaded file type is invalid then a 400 exception will be thrown
     * @param file User uploaded image
     * @return Newly created image
     */
    @Override
    public Image create(MultipartFile file) {
        String filename = generateFilename(file);

        Image image = new Image(null, null);
        image.setFilename(filename);
        image = imageRepository.save(image);

        storageService.store(file, filename);             //store the file using storageService

        return image;
    }

    /**
     * Delete a Image entity and remove the underlying image file
     * @param image Image entity to delete
     */
    @Override
    public void delete(Image image) {
        imageRepository.delete(image);
        storageService.deleteOne(image.getFilename());
    }
}
