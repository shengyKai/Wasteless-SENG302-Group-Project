package org.seng302.leftovers.entities;

import org.hibernate.annotations.Check;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Check(constraints = "(CAST(product_id IS NOT NULL AS int) + CAST(business_id IS NOT NULL AS int) + CAST(user_id IS NOT NULL AS int)) < 2")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant created;

    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Column(name = "filename_thumbnail", nullable = true, unique = false)
    private String filenameThumbnail;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The constructor for a product image
     * @param filename the directory where the image is stored
     * @param filenameThumbnail the directory where the image's thumbnail is located
     */
    public Image(String filename, String filenameThumbnail) {
        created = Instant.now();
        setFilename(filename);
        setFilenameThumbnail(filenameThumbnail);
    }

    /**
     * Empty constructor to make spring happy
     */
    protected Image() {
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
     * Gets the moment in time that the image was made
     * @return Image creation date+time
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * Gets the current entity that owns this image
     * @return Entity that has this image attached
     */
    public ImageAttachment getAttachment() {
        if (product != null) return product;
        if (business != null) return business;
        if (user != null) return user;
        return null;
    }

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
