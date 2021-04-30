package org.seng302.entities;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    public void setFilename(String filename) {
        if (filename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No filename was provided");
        } else if (filename.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An empty filename was provided");
        }
        System.out.println("HMM");
        this.filename = filename;
    }

    /**
     * Sets the direction location of where the image file is located
     * @param filenameThumbnail the directory of where the image thumbnail is located
     */
    //TODO Add validation
    public void setFilenameThumbnail(String filenameThumbnail) {
        if (filenameThumbnail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No thumbnail filename was provided");
        } else if (filenameThumbnail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An empty thumbnail filename was provided");
        }
        this.filenameThumbnail = filenameThumbnail;
    }
}
