package org.seng302.leftovers.service;

import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.tools.ImageTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of StorageService
 */
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final StorageService storageService;

    @Autowired
    public ImageService(ImageRepository imageRepository, StorageService storageService) {
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    /**
     * Validates the provided image's content type and returns the appropriate extension
     * @param contentType The image's MIME type
     * @return Generated filename
     */
    private String getExtension(String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return "jpg";
        } else if ("image/png".equals(contentType)) {
            return "png";
        } else {
            throw new ValidationResponseException("Invalid image format. Must be jpeg or png");
        }
    }

    /**
     * Creates a new image entity from a user upload
     * If the uploaded file type is invalid then a 400 exception will be thrown
     * @param file User uploaded image
     * @return Newly created image
     */
    public Image create(MultipartFile file) {
        String name = UUID.randomUUID().toString();
        String ext = getExtension(file.getContentType());

        BufferedImage original;
        try (InputStream stream = file.getInputStream()) {
            original = ImageIO.read(stream);
        } catch (IOException e) {
            throw new InternalErrorResponseException("Failed get input stream", e);
        }
        if (original == null) {
            throw new ValidationResponseException("Invalid image provided");
        }

        BufferedImage scaled = ImageTools.generateThumbnail(original);
        String filename = String.join(".", name, ext);
        String thumbnailFilename;

        try (InputStream stream = file.getInputStream()) {
            storageService.store(stream, filename);
        } catch (IOException e) {
            throw new InternalErrorResponseException("Failed to save image", e);
        }

        if (scaled == original) { // Scaled is identity to original, therefore original is already thumbnail sized
            thumbnailFilename = filename;
        } else {
            thumbnailFilename = String.join(".", name, "thumb", ext);

            InputStream stream;
            try {
                stream = ImageTools.writeImage(scaled, ext);
            } catch (IOException e) {
                throw new InternalErrorResponseException("Failed to write thumbnail", e);
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
    public void delete(Image image) {
        imageRepository.delete(image);
        storageService.deleteOne(image.getFilename());
        if (image.getFilenameThumbnail() != null && !image.getFilenameThumbnail().equals(image.getFilename())) {
            storageService.deleteOne(image.getFilenameThumbnail());
        }
    }

    /**
     * Converts of image ids into their respective image objects
     * @param imageIds a list of image ids
     * @return a list of image objects
     */
    public List<Image> getListOfImagesFromIds(List<Long> imageIds) {
        List<Image> images = new ArrayList<>();
        for (Long imageId : imageIds) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new DoesNotExistResponseException(Image.class));
            images.add(image);
        }
        return images;
    }
}
