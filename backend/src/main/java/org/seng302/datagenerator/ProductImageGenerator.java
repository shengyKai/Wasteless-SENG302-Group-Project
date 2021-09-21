package org.seng302.datagenerator;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class ProductImageGenerator {

    private Path userUploadsRoot;
    private final Connection conn;
    private Resource[] demoImages;

    public ProductImageGenerator(Connection conn) {
        this.conn = conn;

        userUploadsRoot = Paths.get(ExampleDataFileReader.readPropertiesFile("/application.properties").get("storage-directory"));
        try {
            var loader = ProductImageGenerator.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
            this.demoImages = resolver.getResources("classpath*:org/seng302/datagenerator/example-images/**");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not initialize example images folder.");
        }
    }

    /**
     * Given a product ID and a products name,
     * attempts to create a copy of the image to the images directory
     * and saves the record to the database
     * @param productId The ID of the product
     * @param productName The name of the demo image
     * @throws SQLException
     */
    public void addImageToProduct(Long productId, String productName) throws SQLException {
        String noun = productName.split(" ")[1];
        Optional<Resource> image = findImage(noun);
        if (image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file for product name:" + productName);
        }

        String filename = UUID.randomUUID().toString();
        Optional<String> fileType = getExtension(image.get().getFilename());
        if (fileType.isPresent()) {
            filename += "." + fileType.get();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file type for:" + image.get().toString());
        }

        try {
            store(image.get().getInputStream(), filename);
            createInsertImageSQL(productId, filename);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get input stream from image: " + image.get().getFilename());
        }
    }

    /**
     * Gets the file type from a given file
     * @param filename name of the file
     * @return File type
     */
    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * Given a noun, attempts to find the image associated with that noun.
     * @param noun The product to find
     * @return Optional of type InputStream.
     * @throws IOException
     */
    private Optional<Resource> findImage(String noun) {
        return Arrays.stream(demoImages)
                .filter(res -> Objects.requireNonNull(res.getFilename())
                .replaceFirst("[.][^.]+$", "").equals(noun.toLowerCase(Locale.ROOT)))
                .findFirst();
    }

    /**
     * This is copied and modified from storageServiceImpl
     * Saves a given file to the system
     * @param file The file to save
     * @param filename The name of the new file.
     */
    public void store(InputStream file, String filename) {
        try {
            Files.copy( file, this.userUploadsRoot.resolve(filename));

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    /**
     * Creates and inserts an image record into the database, given an image filename and product id.
     * @param productId The ID of the product related to the new image
     * @param filename The filename of the new image
     * @return The ID of the new image record.
     * @throws SQLException
     */
    private long createInsertImageSQL(Long productId, String filename) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO image (filename, product_id, image_order, filename_thumbnail, created) " +
                "VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setObject(1, filename);
            stmt.setObject(2, productId);
            stmt.setObject(3, 0);
            stmt.setObject(4, filename);
            stmt.setObject(5, Instant.now());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }
}
