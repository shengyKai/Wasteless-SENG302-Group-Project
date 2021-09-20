package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Column(name = "filename_thumbnail", nullable = true, unique = false)
    private String filenameThumbnail;

    /**
     * The constructor for a product image
     * @param filename the directory where the image is stored
     * @param filenameThumbnail the directory where the image's thumbnail is located
     */
    public Image(String filename, String filenameThumbnail) {
        setFilename(filename);
        setFilenameThumbnail(filenameThumbnail);
    }

    /**
     * Empty constructor to make spring happy
     */
    protected Image() {
    }

    public JSONObject constructJSONObject() {
        var object = new JSONObject();
        object.put("id", getID());
        object.put("filename", "/media/images/" + getFilename());
        object.put("thumbnailFilename", "/media/images/" + getFilenameThumbnail());
        return object;
    }

    /**
     * Contains a list of all the image formats that the image can be and checks if the filename is one of the accepted
     * image formats
     * @param filename the name of the directory
     * @return true if the image format is accepted, false otherwise
     */
    private boolean checkImageFormats(String filename) {
        // List of all the image formats an image can be
        final List<String> imageFormats = Arrays.asList(".png", ".jpg");
        if (filename.length() > 4) {
            for (String format : imageFormats) {
                String imageFormat = filename.substring(filename.length() - 4).toLowerCase();
                if (format.equals(imageFormat)) {
                    return true;
                }
            }
        }
        return false;
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
    public void setFilename(String filename) {
        if (filename == null) {
            throw new ValidationResponseException("No filename was provided");
        } else if (filename.isEmpty()) {
            throw new ValidationResponseException("An empty filename was provided");
        } else if (filename.contains(" ")) {
            throw new ValidationResponseException("Spaces are not allowed in the filename");
        } else if (!checkImageFormats(filename)) {
            throw new ValidationResponseException("An invalid image format was provided");
        }
        this.filename = filename;
    }

    /**
     * Sets the direction location of where the image file is located
     * @param filenameThumbnail the directory of where the image thumbnail is located
     */
    public void setFilenameThumbnail(String filenameThumbnail) {
        if (filenameThumbnail == null) {
            throw new ValidationResponseException("No thumbnail filename was provided");
        } else if (filenameThumbnail.isEmpty()) {
            throw new ValidationResponseException("An empty thumbnail filename was provided");
        } else if (filenameThumbnail.contains(" ")) {
            throw new ValidationResponseException("Spaces are not allowed in the thumbnail filename");
        } else if (!checkImageFormats(filenameThumbnail)) {
            throw new ValidationResponseException("An invalid image format was provided");
        }
        this.filenameThumbnail = filenameThumbnail;
    }

    @Override
    public String toString() {
        return "IMAGE_"+this.getID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return id.equals(image.id) && filename.equals(image.filename) && Objects.equals(filenameThumbnail, image.filenameThumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename, filenameThumbnail);
    }
}
