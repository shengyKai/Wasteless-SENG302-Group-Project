package org.seng302.persistence;

import org.seng302.entities.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    /**
     *
     * @param imageDirectory the directory at which the image is stored
     * @return a image found by its associated directory location
     */
    //Image findByDirectory(@Param("imageDirectory") String imageDirectory);
}
