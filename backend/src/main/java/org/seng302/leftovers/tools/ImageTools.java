package org.seng302.leftovers.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageTools {
    private ImageTools() {}

    private static final int THUMBNAIL_SIZE = 128;

    /**
     * Generates a thumbnail from the provided input image.
     * The output image is guaranteed to have both dimensions less than or equal to the 64 pixels.
     * The output image may be the same as the input image if it is already thumbnail size.
     * @param source Image to compute the thumbnail of
     * @return Thumbnail size version of the input image
     */
    public static BufferedImage generateThumbnail(BufferedImage source) {
        int largestDimension = Math.max(source.getWidth(), source.getHeight());
        if (largestDimension <= THUMBNAIL_SIZE) return source;

        float scaleFactor = (float) THUMBNAIL_SIZE / (float)largestDimension;
        int newWidth = Math.round(source.getWidth() * scaleFactor);
        int newHeight = Math.round(source.getHeight() * scaleFactor);

        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, source.getType());

        Graphics2D graphics = thumbnail.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, newWidth, newHeight, 0, 0, source.getWidth(), source.getHeight(), null);
        graphics.dispose();

        return thumbnail;
    }

    /**
     * Converts a BufferedImage to a InputStream
     * @param source Image to write to an InputStream
     * @param format File format of the created InputStream
     * @return InputStream of the image as a file
     * @throws IOException If the image fails to be written
     */
    public static InputStream writeImage(BufferedImage source, String format) throws IOException {
        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(source, format, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
