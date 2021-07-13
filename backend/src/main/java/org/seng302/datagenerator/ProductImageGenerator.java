package org.seng302.datagenerator;

import org.seng302.leftovers.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class ProductImageGenerator {
    private final Path root = Paths.get(ProductImageGenerator.class.getResource("example-images/").toURI());
    private final Path userUploadsRoot = Paths.get("uploads");
    private Connection conn;

    public ProductImageGenerator(Connection conn) throws URISyntaxException {
        try {
            Files.createDirectory(root);
        } catch (FileAlreadyExistsException existsException) {
            // don't do anything if directory exists
        } catch (IOException ioException) {
            throw new RuntimeException("Could not initialize example images folder.");
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
        Optional<File> image = findImage(noun);
        String filename = UUID.randomUUID().toString();
        if (saveImageToSystem(image, filename)) {
            createInsertImageSQL(productId, filename);
        } else {
            System.out.println("File '" + productName + "' Could not be found");
        }
    }

    /**
     * Given a noun, attempts to find the image associated with that noun.
     * @param noun The product to find
     * @return Optional of type File.
     * @throws IOException
     */
    private Optional<File> findImage(String noun) {
        try {
            Optional<Path> foundFile = Files.walk(this.root, 1)
                    .filter(path -> path.getFileName().toString().replaceFirst("[.][^.]+$", "")
                            .equals(noun.toLowerCase(Locale.ROOT))).findFirst();
            return foundFile.map(Path::toFile);
        } catch (IOException e) {
            System.out.println("An error occurred reading an image file:" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred reading an image file:" + e.getMessage());
        }
    }

    /**
     * Given an example image, copies the image into the images directory.
     * @param demoImage The demo image to copy
     * @param fileName The name of the file to save
     * @return true if file successfully saved.
     */
    private boolean saveImageToSystem(Optional<File> demoImage, String fileName) {
        if (demoImage.isPresent()) {
            store(demoImage.get(), fileName);
            return true;
        }
        return false;
    }

    public void store(File file, String filename) {
        try {
            Files.copy( new FileInputStream(file), this.userUploadsRoot.resolve(filename));

        } catch (Exception e) {
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
        stmt.setObject(3, 1);

        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }
}
