package org.seng302.entities;

import javax.persistence.*;

@Table( uniqueConstraints = {
        @UniqueConstraint(columnNames = {"image_directory"})
})

@Entity
public class Image {

    @Id
    @GeneratedValue
    private Long imageId;

    @Column(name = "image_directory", nullable = false, unique = true)
    private String imageDirectory;

    /**
     * The constructor for a product image
     * @param imageDirectory the directory where the image is stored
     */
    public Image(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }

    /**
     * Empty constructor to make spring happy
     */
    protected Image() {

    }

    /**
     * Gets the id associated with the image
     * @return the image's id
     */
    public Long getID() {
        return imageId;
    }

    /**
     * Gets the image directory of where the image is located
     * @return the directory
     */
    public String getImageDirectory() {
        return imageDirectory;
    }

    /**
     * Sets the direction location of where the image file is located
     * @param imageDirectory the directory of where the image is located
     */
    //TODO Add validation
    private void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }
}
