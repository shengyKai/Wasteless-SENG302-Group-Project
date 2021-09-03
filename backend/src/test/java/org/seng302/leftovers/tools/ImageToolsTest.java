package org.seng302.leftovers.tools;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ImageToolsTest {

    /**
     * Fetches a resource and parses it into an image
     * @param resourceName Resource to load
     * @return Image resource
     */
    @SneakyThrows
    private BufferedImage getImageFromResource(String resourceName) {
        return ImageIO.read(Objects.requireNonNull(ImageToolsTest.class.getResourceAsStream(resourceName)));
    }

    @ParameterizedTest
    @CsvSource({
            "big.png,128,128",
            "tall.png,64,128",
            "long.png,128,64"
    })
    void generateThumbnail_imageWithAspectRatio_aspectRatioPreserved(String resourceName, int expectedWidth, int expectedHeight) {
        var image = getImageFromResource(resourceName);
        var scaled = ImageTools.generateThumbnail(image);

        assertEquals(expectedWidth, scaled.getWidth());
        assertEquals(expectedHeight, scaled.getHeight());
    }

    @Test
    void generateThumbnail_imageAlreadyThumbnailSized_originalReturned() {
        var image = getImageFromResource("/point.png");

        var scaled = ImageTools.generateThumbnail(image);
        assertSame(image, scaled);
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpg"})
    void writeImage_validParam_resultCanBeParsedAsOriginal(String format) {
        var image = getImageFromResource("/point.png");

        InputStream stream = assertDoesNotThrow(() -> ImageTools.writeImage(image, format));
        var parsed = assertDoesNotThrow(() -> ImageIO.read(stream));

        assertEquals(1, parsed.getWidth());
        assertEquals(1, parsed.getHeight());
        assertEquals(image.getRGB(0, 0), parsed.getRGB(0, 0));
    }
}
