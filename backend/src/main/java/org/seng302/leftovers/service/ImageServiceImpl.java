package org.seng302.leftovers.service;

import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.tools.ImageTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
     * Validates the provided image's content type and returns the appropriate extension
     * @param file Uploaded file
     * @return Generated filename
     */
    private String getExtension(MultipartFile file) {
        if ("image/jpeg".equals(file.getContentType())) {
            return "jpg";
        } else if ("image/png".equals(file.getContentType())) {
            return "png";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image format. Must be jpeg or png");
        }
    }

    /**
     * Creates a new image entity from a user upload
     * If the uploaded file type is invalid then a 400 exception will be thrown
     * @param file User uploaded image
     * @return Newly created image
     */
    @Override
    public Image create(MultipartFile file) {
        String name = UUID.randomUUID().toString();
        String ext = getExtension(file);

        BufferedImage original;
        try (InputStream stream = file.getInputStream()) {
            original = ImageIO.read(stream);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to interpret input as image", exception);
        }

        BufferedImage scaled = ImageTools.generateThumbnail(original);
        String filename = String.join(".", name, ext);
        String thumbnailFilename;

        try (InputStream stream = file.getInputStream()) {
            storageService.store(stream, filename);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to save image", e);
        }

        if (scaled == original) { // Scaled is identity to original, therefore original is already thumbnail sized
            thumbnailFilename = filename;
        } else {
            thumbnailFilename = String.join(".", name, "thumb", ext);

            InputStream stream;
            try {
                stream = ImageTools.writeImage(scaled, ext);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write thumbnail", e);
            }
            storageService.store(stream, thumbnailFilename);
        }

        Image imageEntity = new Image(filename, thumbnailFilename);
        imageEntity = imageRepository.save(imageEntity);
        return imageEntity;
    }

    /**
     * Delete a Image entity and remove the underlying image file
     * @param image Image entity to delete
     */
    @Override
    public void delete(Image image) {
        imageRepository.delete(image);
        storageService.deleteOne(image.getFilename());
        if (image.getFilenameThumbnail() != null && !image.getFilenameThumbnail().equals(image.getFilename())) {
            storageService.deleteOne(image.getFilenameThumbnail());
        }
    }
}
