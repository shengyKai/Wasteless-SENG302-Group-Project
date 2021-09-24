package cucumber.stepDefinitions;

import io.cucumber.java.en.Given;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

@AutoConfigureMockMvc
public class ImageStepDefinition {
    @Autowired
    private ImageRepository imageRepository;

    @Given("The following images exist:")
    public void the_following_images_exist(List<String> images) {
        for (var name : images) {
            Image image = new Image(name, "thumbnail"+name);
            imageRepository.save(image);
        }

    }
}
