package org.seng302.leftovers.entities;

import java.util.List;

/**
 * Interface for entities that have images attached to them
 */
public interface ImageAttachment {
    /**
     * Gets the images associated with this image attachment point
     * @return List of images
     */
    List<Image> getImages();
}
