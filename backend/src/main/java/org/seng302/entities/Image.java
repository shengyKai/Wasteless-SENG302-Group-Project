package org.seng302.entities;

import javax.persistence.*;

@Table( uniqueConstraints = {
        @UniqueConstraint(columnNames = {"filename", "filename_thumbnail"})
})

@Entity
public class Image {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Column(name = "filename_thumbnail", nullable = false, unique = true)
    private String filenameThumbnail;

    /**
     * The constructor for a product image
     * @param filename the directory where the image is stored
     * @param filenameThumbnail the directory where the image's thumbnail is located
     */
    public Image(String filename, String filenameThumbnail) {
        this.filename = filename;
        this.filenameThumbnail = filenameThumbnail;
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
        return id;
    }

    /**
     * Gets the image directory of where the image is located
     * @return the directory
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the image directory of where the image thumbnail is located
     * @return the directory
     */
    public String getFilenameThumbnail() { return filenameThumbnail; }

    /**
     * Sets the direction location of where the image file is located
     * @param filename the directory of where the image is located
     */
    //TODO Add validation
    private void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Sets the direction location of where the image file is located
     * @param filenameThumbnail the directory of where the image thumbnail is located
     */
    //TODO Add validation
    private void setFilenameThumbnail(String filenameThumbnail) {
        this.filenameThumbnail = filenameThumbnail;
    }
}
