package org.seng302.persistence;

import org.seng302.entities.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    /**
     *
     * @param filename the directory at which the image is stored
     * @return a image found by its associated directory location
     */
    //Image findByDirectory(@Param("filename") String filename);


    Optional<Image> findByFilename(@Param("filename") String filename);
    /**
     * Gets an image from the database that matches a given image Id. This method preforms a sanity check to ensure the
     * image does exist and if not throws a not accepted response status exception.
     * @param imageId the id of the image
     * @return the image object that matches the given Id
     */
    //TODO add unit tests
    default public Image getImage(Long imageId) {
        Optional<Image> image = findById(imageId);
        if (!image.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "the given image does not exist");
        }
        return image.get();
    }
}
