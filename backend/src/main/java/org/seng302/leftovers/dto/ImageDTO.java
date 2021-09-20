package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Image;

/**
 * A DTO representing a image being sent to a client
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageDTO {
    private Long id;
    private String filename;
    private String thumbnailFilename;

    /**
     * Converts a DTO from a given image
     * @param image Image to convert to DTO
     */
    public ImageDTO(Image image) {
        this.id = image.getID();
        this.filename = "/media/images/" + image.getFilename();
        this.thumbnailFilename = "/media/images/" + image.getFilenameThumbnail();
    }

    /**
     * Helper JSON constructor
     */
    protected ImageDTO() {}
}
