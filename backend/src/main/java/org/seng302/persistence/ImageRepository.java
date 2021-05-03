package org.seng302.persistence;

import org.seng302.entities.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /** This is what connor do, I have a differnet approach - Edward
     *
     * @param filename the directory at which the image is stored
     * @return a image found by its associated directory location
     */
    //Image findByDirectory(@Param("filename") String filename);

    
    Optional<Image> findByName(String name);

}
