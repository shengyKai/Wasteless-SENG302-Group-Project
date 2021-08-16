package cucumber.context;

import io.cucumber.java.Before;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class ImageContext {
    private final Map<String, Image> imageMap = new HashMap<>();
    private Image lastImage = null;

    @Autowired
    private ImageRepository imageRepository;

    @Before
    public void setup() {
        imageMap.clear();
        lastImage = null;
    }

    /**
     * Gets the last modified image
     * @return Last modified image
     */
    public Image getLast() {return lastImage;}

    /**
     * Gets an image by filename
     * @param name Image filename to get
     * @return Image with matching filename otherwise null
     */
    public Image getByName(String name) {return imageMap.get(name);}

    /**
     * Saves an image using image repository
     * Will update the last image
     * @param image Image to save
     * @return Saved image
     */
    public Image save(Image image) {
        lastImage = imageRepository.save(image);
        imageMap.put(image.getFilename(), lastImage);
        return lastImage;
    }
}
