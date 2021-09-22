package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    /**
     * Finds the image with the given filename
     * @param filename Filename to find
     * @return Relevant image entity
     */
    Optional<Image> findByFilename(@Param("filename") String filename);

    /**
     * Finds the image with the given thumbnail filename
     * @param filenameThumbnail Filename to find
     * @return Relevant image entity
     */
    Optional<Image> findByFilenameThumbnail(@Param("filenameThumbnail") String filenameThumbnail);

    /**
     * Gets an image from the database that matches a given image Id. This method preforms a sanity check to ensure the
     * image does exist and if not throws a not accepted response status exception.
     * @param imageId the id of the image
     * @return the image object that matches the given Id
     */
    default Image getImageById(Long imageId) {
        Optional<Image> image = findById(imageId);
        if (image.isEmpty()) {
            throw new DoesNotExistResponseException(Image.class);
        }
        return image.get();
    }
    /**
     * Gets an image for a product by Product and ID
     * Assures that if an image no longer belongs on the Product then no image is returned.
     * If the image does not exist or does not belong to a product, a 406 Not Acceptable is thrown
     * @param product The product
     * @param imageId The ID of the image to fetch
     * @return An Image or ResponseStatusException
     */
    default Image getImageByProductAndId(Product product, Long imageId) {
        Optional<Image> image = this.findById(imageId);
        if (image.isEmpty() || !product.getImages().contains(image.get())) {
            throw new DoesNotExistResponseException(Image.class);
        }
        return image.get();

    }

    default List<Image> getImagesByIds(List<Long> ids) {
        List<Image> images = new ArrayList<>();
        for (var id: ids) {
            images.add(getImageById(id));
        }
        return images;
    }
}
