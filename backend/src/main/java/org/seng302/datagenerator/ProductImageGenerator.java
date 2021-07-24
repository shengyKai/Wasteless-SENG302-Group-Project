package org.seng302.datagenerator;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductImageGenerator {

    private final Path userUploadsRoot = Paths.get("uploads");
    private final Connection conn;
    private Resource[] demoImages;

    public ProductImageGenerator(Connection conn) {
        try {
            var loader = ProductImageGenerator.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
            this.demoImages = resolver.getResources("classpath*:org/seng302/datagenerator/example-images/**");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not initialize example images folder.");
        }
        this.conn = conn;
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
        Optional<InputStream> image = findImage(noun);
        if (image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file for product name:" + productName);
        }

        String filename = UUID.randomUUID().toString();
        Optional<String> fileType = getExtension(image.get().toString());
        if (fileType.isPresent()) {
            filename += "." + fileType.get();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file type for:" + image.get().toString());
        }

        if (saveImageToSystem(image.get(), filename)) {
            createInsertImageSQL(productId, filename);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could save image to disk for: " + image.get().toString());
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
    private Optional<InputStream> findImage(String noun) {
        Optional<Resource> file = Arrays.stream(demoImages)
                .filter(res -> Objects.requireNonNull(res.getFilename())
                .replaceFirst("[.][^.]+$", "").equals(noun.toLowerCase(Locale.ROOT)))
                .findFirst();

        return file.map(resource -> {
            try {
                return resource.getInputStream();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred reading an image file:" + e.getMessage());
            }
        });
    }

    /**
     * Given an example image, copies the image into the images directory.
     * @param demoImage The demo image to copy
     * @param fileName The name of the file to save
     * @return true if file successfully saved.
     */
    private boolean saveImageToSystem(InputStream demoImage, String fileName) {
        try {
            store(demoImage, fileName);
            return true;
        } catch (Exception e) {
            return false;
        }
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
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO image (filename, image_id, image_order) " +
                "VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, filename);
        stmt.setObject(2, productId);
        stmt.setObject(3, 0);

        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }
}